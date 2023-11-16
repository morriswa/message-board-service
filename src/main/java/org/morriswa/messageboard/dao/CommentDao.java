package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;

import java.util.List;

public interface CommentDao {
    List<Comment> findAllCommentsByPostId(Long postId);

    void createNewComment(CommentRequest newCommentRequest);
}
