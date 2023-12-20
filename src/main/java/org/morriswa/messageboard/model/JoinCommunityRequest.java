package org.morriswa.messageboard.model;

import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.util.UUID;

public record JoinCommunityRequest (
        UUID userId, Long communityId, ModerationLevel moderationLevel, CommunityStanding communityStanding
)
{
}
