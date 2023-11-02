package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Comment;

import java.util.List;

public interface CommentDao {
    List<Comment> findAllCommentsByPostId(Long postId);

    void createNewComment(Comment newComment);
}
