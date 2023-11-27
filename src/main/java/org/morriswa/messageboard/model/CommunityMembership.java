package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.util.UUID;

@Getter @AllArgsConstructor
public class CommunityMembership {
    private final boolean exists;
    private final UUID userId;
    private final Long communityId;
    private final CommunityStanding standing;
    private final ModerationLevel moderationLevel;
}