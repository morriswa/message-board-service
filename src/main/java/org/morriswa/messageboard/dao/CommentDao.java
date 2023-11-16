package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentDao {
    List<Comment> findAllCommentsByPostId(Long postId);

    void createNewComment(CommentRequest newCommentRequest);

    void vote(UUID userId, Long postId, Long commentId, Vote vote);
}
