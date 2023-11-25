package org.morriswa.messageboard.validation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;


@Valid @AllArgsConstructor @Getter
public class CreateCommunityRequest {

    @NotBlank
    private String communityLocator;

    @NotBlank
    private String communityDisplayName;

    @NotNull
    private UUID communityOwnerUserId;
}
