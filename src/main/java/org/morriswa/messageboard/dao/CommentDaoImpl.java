package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.timestampToGregorian;

@Component
public class CommentDaoImpl implements CommentDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public CommentDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private List<Comment> extractComments(ResultSet rs) throws SQLException {
        List<Comment> comments = new ArrayList<>();

        while (rs.next()) {
            comments.add(
                    new Comment(
                            rs.getLong("id"),
                            rs.getObject("user_id", UUID.class),
                            rs.getLong("post_id"),
                            rs.getLong("parent_id"),
                            rs.getString("body"),
                            rs.getInt("count"),
                            timestampToGregorian(rs.getTimestamp("date_created"))
                    )
            );
        }

        return comments;
    }
    
    @Override
    public List<Comment> findComments(Long postId) {
        final String query = """
            select
                *,
                (select sum(cvt.vote_value) from comment_vote cvt where cvt.comment_id=post_comment.id) AS count
                from post_comment where post_id = :postId and parent_id = -1;
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
        }};

        return jdbc.query(query, params, this::extractComments);
    }

    @Override
    public List<Comment> findComments(Long postId, Long parentId) {
        final String query = """
            select
                *,
                (select sum(cvt.vote_value) from comment_vote cvt where cvt.comment_id=post_comment.id) AS count
                from post_comment where post_id = :postId and parent_id = :parentId;
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
            put("parentId", parentId);
        }};

        return jdbc.query(query, params, this::extractComments);
    }

    @Override
    public void createNewComment(CommentRequest newCommentRequest) {
        final String query = """
            insert into post_comment (id, user_id, post_id, parent_id, body, date_created)
            values(default, :userId, :postId, :parentId, :body, current_timestamp)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", newCommentRequest.getUserId());
            put("postId", newCommentRequest.getPostId());
            put("parentId", newCommentRequest.getParentCommentId());
            put("body", newCommentRequest.getCommentBody());
        }};

        jdbc.update(query, params);
    }

    @Override
    public void vote(UUID userId, Long postId, Long commentId, Vote vote) {
        final String existsQuery = """
                select 1 from comment_vote where user_id=:userId and post_id=:postId and comment_id=:commentId
            """;

        final String deleteQuery = """
                delete from comment_vote where user_id=:userId and post_id=:postId and comment_id=:commentId
            """;

        final String updateQuery = """
                update comment_vote set
                    vote_value = :voteValue,
                    date_created = current_timestamp
                where user_id=:userId and post_id=:postId and comment_id=:commentId
            """;

        final String createQuery = """
                    insert into comment_vote (id, user_id, post_id, comment_id, vote_value, date_created)
                    values(DEFAULT, :userId, :postId, :commentId, :voteValue, current_timestamp)
                """;
        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
        }};

        Map<String, Object> voteParams = new HashMap<>() {{
            put("userId", userId);
            put("postId", postId);
            put("commentId", commentId);
            put("voteValue", vote.weight);
        }};
        
        if (vote.equals(Vote.DELETE))
        {
            jdbc.update(deleteQuery, params);
            return;
        }

        final boolean userAlreadyVoted = Boolean.TRUE.equals(jdbc.query(existsQuery, params, ResultSet::next));

        if (userAlreadyVoted) {
            jdbc.update(updateQuery, voteParams);
        }
        else {
            jdbc.update(createQuery, voteParams);
        }

    }

}
