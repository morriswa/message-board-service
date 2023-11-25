package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.validation.request.CreatePostRequest;
import org.morriswa.messageboard.model.Post;
import org.morriswa.messageboard.enumerated.PostContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.timestampToGregorian;

@Component @Slf4j
public class PostDaoImpl implements PostDao{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public PostDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Post> findPostByPostId(Long postId) {
        final String query = """
            select
                *,
                (select sum(pvt.vote_value) from post_vote pvt where pvt.post_id=:postId) AS count
                from user_post where id=:postId
        """;
        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
        }};

        return jdbcTemplate.query(query, params, rs -> {
            if (rs.next()) {
                return Optional.of(new Post(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getString("caption"),
                        rs.getString("description"),
                        PostContentType.valueOf(rs.getString("content_type")),
                        timestampToGregorian(rs.getTimestamp("date_created")),
                        rs.getObject("resource_id", UUID.class),
                        rs.getInt("count")
                ));
            }

            return Optional.empty();
        });
    }

    @Override
    public List<Post> findAllPostsByCommunityId(Long communityId) {
        final String query = """
            select
                *,
                (select sum(pvt.vote_value) from post_vote pvt where pvt.post_id=user_post.id) AS count
                from user_post where community_id=:communityId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
        }};

        return jdbcTemplate.query(query, params, rs -> {
            List<Post> createPostRequests = new ArrayList<>();

            while (rs.next()) {
                createPostRequests.add(new Post(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getString("caption"),
                        rs.getString("description"),
                        PostContentType.valueOf(rs.getString("content_type")),
                        timestampToGregorian(rs.getTimestamp("date_created")),
                        rs.getObject("resource_id", UUID.class),
                        rs.getInt("count")
                ));
            }

            return createPostRequests;
        });
    }

    @Override
    public List<Post> getMostRecent() {
        return this.getMostRecent(10);
    }

    @Override
    public List<Post> getMostRecent(int endSlice) {
        final String query = """
            select
                *,
                (select sum(pvt.vote_value) from post_vote pvt where pvt.post_id=user_post.id) AS vote_count
                from user_post order by date_created desc limit :endSlice
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("endSlice", endSlice);
        }};

        return jdbcTemplate.query(query, params, rs -> {
            List<Post> createPostRequests = new ArrayList<>();

            while (rs.next()) {
                createPostRequests.add(new Post(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getString("caption"),
                        rs.getString("description"),
                        PostContentType.valueOf(rs.getString("content_type")),
                        timestampToGregorian(rs.getTimestamp("date_created")),
                        rs.getObject("resource_id", UUID.class),
                        rs.getInt("vote_count")
                ));
            }

            return createPostRequests;
        });
    }

    @Override
    public List<Post> getMostRecent(int startSlice, int endSlice) {
        final String query = """
            select
                *,
                (select sum(pvt.vote_value) from post_vote pvt where pvt.post_id=user_post.id) AS vote_count
                from user_post order by date_created desc limit :selects offset :startSlice
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("startSlice", startSlice);
            put("selects", endSlice - startSlice);
        }};

        return jdbcTemplate.query(query, params, rs -> {
            List<Post> createPostRequests = new ArrayList<>();

            while (rs.next()) {
                createPostRequests.add(new Post(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getString("caption"),
                        rs.getString("description"),
                        PostContentType.valueOf(rs.getString("content_type")),
                        timestampToGregorian(rs.getTimestamp("date_created")),
                        rs.getObject("resource_id", UUID.class),
                        rs.getInt("vote_count")
                ));
            }

            return createPostRequests;
        });
    }


    @Override
    public void createNewPost(@Valid CreatePostRequest newCreatePostRequest) {
        final String query =
            """
                insert into user_post(id, user_id, community_id, caption, description, date_created, content_type, resource_id)
                values(DEFAULT, :userId, :communityId, :caption, :description, current_timestamp, :contentType, :resourceId)
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", newCreatePostRequest.getUserId());
            put("communityId", newCreatePostRequest.getCommunityId());
            put("caption", newCreatePostRequest.getCaption());
            put("description", newCreatePostRequest.getDescription());
            put("contentType", newCreatePostRequest.getPostContentType().toString());
            put("resourceId", newCreatePostRequest.getResourceId());
        }};

        jdbcTemplate.update(query, params);
    }


    @Override
    public int vote(UUID userId, Long postId, Vote vote) {
        final String existsQuery = """
            select 1 from post_vote where user_id=:userId and post_id=:postId
        """;

        final String countQuery = """
            select sum(pvt.vote_value) as count from post_vote pvt where pvt.post_id=:postId
        """;

        final String updateQuery = """
                update post_vote set
                    vote_value = :voteValue,
                    date_created = current_timestamp
                where user_id=:userId and post_id=:postId
            """;

        final String createQuery = """
                insert into post_vote (id, user_id, post_id, vote_value, date_created)
                values(DEFAULT, :userId, :postId, :voteValue, current_timestamp)
            """;

        final String deleteQuery = """
                delete from post_vote where user_id=:userId and post_id=:postId
            """;

        final Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
        }};

        final Map<String, Object> voteParams = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("voteValue", vote.weight);
        }};

        final Map<String, Object> countParams = new HashMap<>() {{
            put("postId", postId);
        }};

        if (vote.equals(Vote.DELETE))
        {
            jdbcTemplate.update(deleteQuery, params);
        } else {
            boolean userAlreadyVoted = Boolean.TRUE.equals(jdbcTemplate.query(existsQuery, params, ResultSet::next));

            if (userAlreadyVoted) jdbcTemplate.update(updateQuery, voteParams);
            else jdbcTemplate.update(createQuery, voteParams);
        }

        Optional<Integer> newCount = jdbcTemplate.query(countQuery, countParams, rs -> {
            if (rs.next())
                return Optional.of(rs.getInt("count"));
            return Optional.empty();
        });

        Objects.requireNonNull(newCount);

        return newCount.orElse(0);
    }
}
