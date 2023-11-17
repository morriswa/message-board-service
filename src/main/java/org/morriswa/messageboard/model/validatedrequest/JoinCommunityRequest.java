package org.morriswa.messageboard.model.validatedrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.CommunityStanding;

import java.util.UUID;

@Valid @Getter
public class JoinCommunityRequest {

    @NotNull
    private Long communityId;

    @NotNull
    private UUID userId;

    @NotNull
    private Integer moderationLevel;

    @NotNull
    private CommunityStanding communityStanding;

    public JoinCommunityRequest(UUID userId, Long communityId) {
        this.userId = userId;
        this.communityId = communityId;
        this.communityStanding = CommunityStanding.HEALTHY;
        this.moderationLevel = 0;
    }

}
