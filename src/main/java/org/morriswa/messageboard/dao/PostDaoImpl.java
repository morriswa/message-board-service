package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.validatedrequest.CreatePostRequest;
import org.morriswa.messageboard.model.entity.Post;
import org.morriswa.messageboard.model.PostContentType;
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
//            log.info("located {} posts in community {}", posts.size(), communityId);

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

    private void deleteVote(UUID userId, Long postId) {
        final String query = """
                delete from post_vote where user_id=:userId and post_id=:postId
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("postId", postId);
        }};

        jdbcTemplate.update(query, params);
    }

    private void createVote(UUID userId, Long postId, Vote vote) {
        final String query = """
                insert into post_vote (id, user_id, post_id, vote_value)
                values(DEFAULT, :userId, :postId, :voteValue)
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("voteValue", vote.weight);
        }};

        jdbcTemplate.update(query, params);
    }

    private void updateVote(UUID userId, Long postId, Vote vote) {
        final String query = """
                update post_vote 
                    set vote_value = :voteValue
                where user_id=:userId and post_id=:postId
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("voteValue", vote.weight);
        }};

        jdbcTemplate.update(query, params);
    }

    @Override
    public void vote(UUID userId, Long postId, Vote vote) {

        if (vote.equals(Vote.DELETE))
        {
            deleteVote(userId, postId);
            return;
        }

        final String query = """
            select 1 from post_vote where user_id=:userId and post_id=:postId
        """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
        }};

        boolean userAlreadyVoted = Boolean.TRUE.equals(jdbcTemplate.query(query, params, ResultSet::next));

        if (userAlreadyVoted) updateVote(userId, postId, vote);
        else createVote(userId, postId, vote);
    }
}
