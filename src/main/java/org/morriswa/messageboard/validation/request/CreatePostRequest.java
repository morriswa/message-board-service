package org.morriswa.messageboard.validation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.util.UUID;

@Valid @AllArgsConstructor @Getter
public class CreatePostRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Long communityId;

    @NotBlank
    private String caption;

    private String description;

    @NotNull
    private PostContentType postContentType;

    @NotNull
    private UUID resourceId;
}
