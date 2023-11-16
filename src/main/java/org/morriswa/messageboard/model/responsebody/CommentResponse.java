//package org.morriswa.messageboard.model.responsebody;
//
//import lombok.Data;
//import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Data
//public class CommentResponse {
//
//    private Long commentId;
//
//    private UUID userId;
//
//    private Long postId;
//
//    private Long parentCommentId;
//
//    private String commentBody;
//
//    private List<CommentResponse> subComments;
//
//    public CommentResponse(CommentRequest commentRequestFromDb) {
//        this.commentId = commentRequestFromDb.getCommentId();
//        this.userId = commentRequestFromDb.getUserId();
//        this.postId = commentRequestFromDb.getPostId();
//        this.parentCommentId = commentRequestFromDb.getParentCommentId();
//        this.commentBody = commentRequestFromDb.getCommentBody();
//        this.subComments = new ArrayList<>();
//    }
//}
