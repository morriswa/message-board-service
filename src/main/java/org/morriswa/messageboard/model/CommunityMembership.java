package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.entity.CommunityMember;

import java.util.UUID;

@Getter
public class CommunityMembership {
    private final boolean exists;
    private final UUID userId;
    private final Long communityId;
    private final CommunityStanding standing;

    public CommunityMembership() {
        this.exists = false;
        this.userId = null;
        this.communityId = null;
        this.standing = null;
    }

    public CommunityMembership(UUID userId, Long communityId, CommunityStanding standing) {
        this.exists = true;
        this.userId = userId;
        this.communityId = communityId;
        this.standing = standing;
    }
}
