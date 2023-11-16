package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
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
            select
                *,
                (select sum(cvt.vote_value) from comment_vote cvt where cvt.comment_id=post_comment.id) AS count
                from post_comment where post_id = :postId and parent_id = -1;
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
                        rs.getString("body"),
                        rs.getInt("count")
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

    private void deleteVote(UUID userId, Long postId, Long commentId) {
        final String query = """
                delete from comment_vote where user_id=:userId and post_id=:postId and comment_id=:commentId
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
        }};

        jdbc.update(query, params);
    }

    private void createVote(UUID userId, Long postId, Long commentId, Vote vote) {
        final String query = """
                insert into comment_vote (id, user_id, post_id, comment_id, vote_value)
                values(DEFAULT, :userId, :postId, :commentId, :voteValue)
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
            put("voteValue", vote.weight);
        }};

        jdbc.update(query, params);
    }

    private void updateVote(UUID userId, Long postId, Long commentId, Vote vote) {
        final String query = """
                update comment_vote
                    set vote_value = :voteValue
                where user_id=:userId and post_id=:postId and comment_id=:commentId
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
            put("voteValue", vote.weight);
        }};

        jdbc.update(query, params);
    }

    @Override
    public void vote(UUID userId, Long postId, Long commentId, Vote vote) {
        if (vote.equals(Vote.DELETE))
        {
            deleteVote(userId, postId, commentId);
            return;
        }

        final String query = """
            select 1 from comment_vote where user_id=:userId and post_id=:postId and comment_id=:commentId
        """;

        Map<String, Object> params = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
        }};

        boolean userAlreadyVoted = Boolean.TRUE.equals(jdbc.query(query, params, ResultSet::next));

        if (userAlreadyVoted) updateVote(userId, postId, commentId, vote);
        else createVote(userId, postId, commentId, vote);
    }
}
