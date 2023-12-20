package org.morriswa.messageboard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public record CommentRequest (
    @NotNull UUID userId,

    @NotNull Long postId,

    @NotNull Long parentCommentId,

    @NotBlank String commentBody

) {
    public static CommentRequest buildSubCommentRequest(UUID userId, Long postId, Long parentCommentId, String commentBody) {
        return new CommentRequest(userId, postId, parentCommentId, commentBody);
    }

    public static CommentRequest buildCommentRequest(UUID userId, Long postId, String commentBody) {
        return new CommentRequest(userId, postId, -1L, commentBody);
    }
}
