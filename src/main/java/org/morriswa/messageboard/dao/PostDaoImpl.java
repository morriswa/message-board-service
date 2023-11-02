package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.Post;
import org.morriswa.messageboard.model.PostContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

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
        return Optional.empty();
    }

    @Override
    public List<Post> findAllPostsByCommunityId(Long communityId) {
        final String query = "select * from user_post where community_id=:communityId";

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
        }};

        return jdbcTemplate.query(query, params, rs -> {
            List<Post> posts = new ArrayList<>();

            while (rs.next()) {
                posts.add(new Post(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getString("caption"),
                        rs.getString("description"),
                        timestampToGregorian(rs.getTimestamp("date_created")),
                        PostContentType.valueOf(rs.getString("content_type")),
                        rs.getObject("resource_id", UUID.class)
                ));
            }
//            log.info("located {} posts in community {}", posts.size(), communityId);

            return posts;
        });
    }

    @Override
    public void createNewPost(@Valid Post newPost) {
        final String query =
            """
                insert into user_post(id, user_id, community_id, caption, description, date_created, content_type, resource_id)
                values(DEFAULT, :userId, :communityId, :caption, :description, current_timestamp, :contentType, :resourceId)
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", newPost.getUserId());
            put("communityId", newPost.getCommunityId());
            put("caption", newPost.getCaption());
            put("description", newPost.getDescription());
            put("contentType", newPost.getPostContentType().toString());
            put("resourceId", newPost.getResourceId());
        }};

        jdbcTemplate.update(query, params);
    }
}
