package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.validation.request.CommentRequest;
import org.morriswa.messageboard.model.Comment;
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

    private static final String GET_SUBCOMMENTS_SQL = """
        select id from post_comment
        where post_id=:postId and parent_id=:commentId
    """;

    private static final String BATCH_DEL_COMMENTS_SQL = """
        delete from post_comment
        where post_id=:postId and id in (:marked_for_del)
    """;

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
                            rs.getString("display_name"),
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
                pc.id id,
                pc.user_id user_id,
                up.display_name display_name,
                pc.post_id post_id,
                pc.parent_id parent_id,
                pc.body body,
                pc.date_created date_created,
                (select sum(cvt.vote_value) from comment_vote cvt where cvt.comment_id=pc.id) AS count
            from post_comment pc
                join user_profile up on pc.user_id = up.id
            where post_id = :postId and parent_id = -1
            order by pc.date_created desc
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
                pc.id id,
                pc.user_id user_id,
                up.display_name display_name,
                pc.post_id post_id,
                pc.parent_id parent_id,
                pc.body body,
                pc.date_created date_created,
                (select sum(cvt.vote_value) from comment_vote cvt where cvt.comment_id=pc.id) AS count
            from post_comment pc
                join user_profile up on up.id=pc.user_id
            where post_id = :postId and parent_id = :parentId
            order by pc.date_created desc
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
    public int vote(UUID userId, Long postId, Long commentId, Vote vote) {
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

        final String countQuery = """
                select sum(vote_value) as count from comment_vote where post_id=:postId and comment_id=:commentId
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
        } else {
            final boolean userAlreadyVoted = Boolean.TRUE.equals(jdbc.query(existsQuery, params, ResultSet::next));

            if (userAlreadyVoted) {
                jdbc.update(updateQuery, voteParams);
            }
            else {
                jdbc.update(createQuery, voteParams);
            }
        }

        return jdbc.query(countQuery, params, rs -> {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        });
    }

    @Override
    public void deletePostComments(Long postId) {
        final String deleteQuery = """
            delete from post_comment where post_id=:postId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
        }};

        jdbc.update(deleteQuery, params);
    }



    private void deleteSubcomments(Long postId, Long commentId) {

        Map<String, Object> subcommentParams = new HashMap<>(){{
            put("postId", postId);
            put("commentId",commentId);
        }};

        List<Long> subcomments = jdbc.query(GET_SUBCOMMENTS_SQL, subcommentParams, rs -> {
            List<Long> ids = new ArrayList<>();

            while (rs.next())
                ids.add(rs.getLong("id"));

            return ids;
        });

        assert subcomments != null;
        // if no subcomments are retrieved, continue with next iteration
        if (subcomments.isEmpty()) return;

        Map<String, Object> batchDeleteParams = new HashMap<>(){{
            put("postId", postId);
            // mark all subcomments for deletion
            put("marked_for_del",subcomments);
        }};

        jdbc.update(BATCH_DEL_COMMENTS_SQL, batchDeleteParams);

        // recursively continue deleting subcomments
        subcomments.forEach(subCommentFound->deleteSubcomments(postId, subCommentFound));
    }

    @Override
    public void deleteCommentAndChildren(Long postId, Long commentId) {
        deleteSubcomments(postId, commentId);

        final String deleteQuery = """
                delete from post_comment
                where post_id=:postId and id=:commentId
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("postId", postId);
            put("commentId",commentId);
        }};

        jdbc.update(deleteQuery, params);
    }

}
