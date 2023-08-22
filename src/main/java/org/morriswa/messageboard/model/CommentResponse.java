package org.morriswa.messageboard.model;

import lombok.Data;
import org.morriswa.messageboard.entity.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CommentResponse {

    private Long commentId;

    private UUID userId;

    private Long postId;

    private Long parentCommentId;

    private String commentBody;

    private List<Object> subComments;

    public CommentResponse(Comment commentFromDb) {
        this.commentId = commentFromDb.getCommentId();
        this.userId = commentFromDb.getUserId();
        this.postId = commentFromDb.getPostId();
        this.parentCommentId = commentFromDb.getParentCommentId();
        this.commentBody = commentFromDb.getCommentBody();
        this.subComments = new ArrayList<>();
    }
}
