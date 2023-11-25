package org.morriswa.messageboard.validation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Valid
@AllArgsConstructor @Getter
public class CommentRequest {

    @NotNull
    private final UUID userId;

    @NotNull
    private final Long postId;

    @NotNull
    private final Long parentCommentId;

    @NotBlank
    private final String commentBody;

    public static CommentRequest buildSubCommentRequest(UUID userId, Long postId, Long parentCommentId, String commentBody) {
        return new CommentRequest(userId, postId, parentCommentId, commentBody);
    }

    public static CommentRequest buildCommentRequest(UUID userId, Long postId, String commentBody) {
        return new CommentRequest(userId, postId, -1L, commentBody);
    }
}
