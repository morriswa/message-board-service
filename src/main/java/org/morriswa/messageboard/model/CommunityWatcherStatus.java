package org.morriswa.messageboard.model;

import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.util.UUID;

public record CommunityWatcherStatus(
        boolean exists,
        UUID userId,
        Long communityId,
        CommunityStanding standing,
        ModerationLevel moderationLevel
) { }
