package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class NewCommentRequest {
    private Long parentCommentId;
    private Long postId;
    private String comment;
}
