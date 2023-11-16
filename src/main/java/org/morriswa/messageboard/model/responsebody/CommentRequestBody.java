package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CommentRequestBody {
    private Long parentCommentId;
    private Long postId;
    private String comment;
}
