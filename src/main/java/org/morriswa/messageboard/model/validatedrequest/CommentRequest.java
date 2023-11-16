package org.morriswa.messageboard.model.validatedrequest;

//import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

//@Entity @Table(name = "user_comments")
@Valid
@AllArgsConstructor @Getter
public class CommentRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Long postId;

    @NotNull
    private Long parentCommentId;

    @NotBlank
    @Length(max = 5000)
    private String commentBody;

    public static CommentRequest buildSubCommentRequest(UUID userId, Long postId, Long parentCommentId, String commentBody) {
        return new CommentRequest(userId, postId, parentCommentId, commentBody);
    }

    public static CommentRequest buildCommentRequest(UUID userId, Long postId, String commentBody) {
        return new CommentRequest(userId, postId, -1L, commentBody);
    }
}
