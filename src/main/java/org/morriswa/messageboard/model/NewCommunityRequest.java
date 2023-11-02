package org.morriswa.messageboard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor @Data @AllArgsConstructor
@Builder
public class NewCommunityRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]*$")
    private String communityLocator;

    @NotBlank
    private String communityDisplayName;

    @NotNull
    private UUID communityOwnerUserId;
}
