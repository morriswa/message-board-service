package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.util.GregorianCalendar;
import java.util.UUID;

@AllArgsConstructor @Getter
public class CommunityMember {
    private final UUID userId;
    private final String displayName;
    private final String email;
    private final ModerationLevel moderationLevel;
    private final CommunityStanding communityStanding;
    private final GregorianCalendar dateUpdated;
    private final GregorianCalendar dateJoined;
}
