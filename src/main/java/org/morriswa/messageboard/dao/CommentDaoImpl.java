package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommentDaoImpl implements CommentDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public CommentDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Comment> findAllCommentsByPostId(Long postId) {
        final String query = """
            select * from post_comment where post_id = :postId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
        }};

        return jdbc.query(query, params, rs -> {
            List<Comment> comments = new ArrayList<>();

            while (rs.next()) {
                comments.add(
                    new Comment(
                        rs.getLong("id"),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("post_id"),
                        rs.getLong("parent_id"),
                        rs.getString("body")
                    )
                );
            }

            return comments;
        });
    }

    @Override
    public void createNewComment(CommentRequest newCommentRequest) {
        final String query = """
            insert into post_comment (id, user_id, post_id, parent_id, body)
            values(default, :userId, :postId, :parentId, :body)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", newCommentRequest.getUserId());
            put("postId", newCommentRequest.getPostId());
            put("parentId", newCommentRequest.getParentCommentId());
            put("body", newCommentRequest.getCommentBody());
        }};

        jdbc.update(query, params);
    }
}
