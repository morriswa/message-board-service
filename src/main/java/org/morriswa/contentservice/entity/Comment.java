package org.morriswa.contentservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity @Table(name = "user_comments")
@NoArgsConstructor @Getter @Builder
@AllArgsConstructor
public class Comment {
    @Id
    @SequenceGenerator(name = "user_comments_seq_qen", sequenceName = "user_comments_seq")
    @GeneratedValue(generator = "user_comments_seq_gen", strategy = GenerationType.AUTO)
    private Long commentId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private UUID userId;

    @NotNull
    @Column(name = "post_id", nullable = false, updatable = false)
    private Long postId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @NotBlank
    @Length(max = 5000)
    @Column(nullable = false)
    private String commentBody;
}
