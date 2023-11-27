package org.morriswa.messageboard.validation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.util.UUID;

@Valid @Getter
public class JoinCommunityRequest {

    @NotNull
    private Long communityId;

    @NotNull
    private UUID userId;

    @NotNull
    private ModerationLevel moderationLevel;

    @NotNull
    private CommunityStanding communityStanding;

    public JoinCommunityRequest(UUID userId, Long communityId) {
        this.userId = userId;
        this.communityId = communityId;
        this.communityStanding = CommunityStanding.HEALTHY;
        this.moderationLevel = ModerationLevel.NONE;
    }
}
