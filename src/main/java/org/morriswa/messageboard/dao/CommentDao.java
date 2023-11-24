package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentDao {
    List<Comment> findComments(Long postId);

    List<Comment> findComments(Long postId, Long parentId);

    void createNewComment(CommentRequest newCommentRequest);

    int vote(UUID userId, Long postId, Long commentId, Vote vote);

}
