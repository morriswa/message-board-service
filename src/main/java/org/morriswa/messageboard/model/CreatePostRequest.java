package org.morriswa.messageboard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.util.UUID;

@Valid
public record CreatePostRequest (
    @NotNull UUID userId,
    @NotNull Long communityId,
    @NotBlank String caption,
    String description,
    @NotNull PostContentType postContentType,
    @NotNull UUID resourceId
){}
