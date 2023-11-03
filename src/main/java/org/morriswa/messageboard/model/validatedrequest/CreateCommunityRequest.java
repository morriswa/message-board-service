package org.morriswa.messageboard.model.validatedrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;


@Valid @AllArgsConstructor @Getter
public class CreateCommunityRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]*$")
    private String communityLocator;

    @NotBlank
    private String communityDisplayName;

    @NotNull
    private UUID communityOwnerUserId;
}