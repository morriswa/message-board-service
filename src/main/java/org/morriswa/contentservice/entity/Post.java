package org.morriswa.contentservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.morriswa.contentservice.model.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

@Entity @Table(name = "user_post")
@NoArgsConstructor @Getter
public class Post {
    @Id
    @SequenceGenerator(name = "user_post_seq_gen", sequenceName = "user_post_seq")
    @GeneratedValue(generator = "user_post_seq_gen",strategy = GenerationType.AUTO)
    private Long postId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private UUID userId;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Long communityId;

    @NotBlank
    @Length(min=5, max = 100)
    @Column(nullable = false, updatable = false)
    private String caption;

    @Length(max = 10000)
    @Column(updatable = false)
    private String description;

    @NotNull
    @Column(nullable = false, updatable = false)
    private GregorianCalendar dateCreated;

    @NotNull
    @Column(nullable = false, updatable = false)
    private PostContentType postContentType;

    @NotNull
    @Column(nullable = false, updatable = false, unique = true)
    private UUID resourceId;

    public Post(UUID userId,
    Long communityId, 
    String caption, 
    String description, 
    PostContentType contentType, 
    UUID resourceId) {
        this.userId = userId;
        this.communityId = communityId;
        this.caption = caption;
        this.description = description;
        this.postContentType = contentType;
        this.resourceId = resourceId;
        this.dateCreated = new GregorianCalendar();
    }
}
