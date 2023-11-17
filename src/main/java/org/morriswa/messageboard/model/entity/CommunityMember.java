package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.CommunityStanding;

import java.util.GregorianCalendar;
import java.util.UUID;

@AllArgsConstructor @Getter
public class CommunityMember {
    private final Long relationshipId;
    private final Long communityId;
    private final UUID userId;
    private final Integer moderationLevel;
    private final CommunityStanding communityStanding;
    private final GregorianCalendar relationshipLastUpdatedDate;
    private final GregorianCalendar joinDate;
}
