package org.morriswa.messageboard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.GregorianCalendar;
import java.util.UUID;

@NoArgsConstructor @Getter
public class CreatePostRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Long communityId;

    @NotBlank
    @Length(min=5, max = 100)
    private String caption;

    @Length(max = 10000)
    private String description;

    @NotNull
    private GregorianCalendar dateCreated;

    @NotNull
    private PostContentType postContentType;

    @NotNull
    private UUID resourceId;

    public CreatePostRequest(UUID userId,
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
