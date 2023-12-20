package org.morriswa.messageboard.model;

import jakarta.validation.constraints.NotNull;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public record JoinCommunityRequest (
        @NotNull UUID userId,
        @NotNull Long communityId,
        @NotNull ModerationLevel moderationLevel,
        @NotNull CommunityStanding communityStanding
) { }
