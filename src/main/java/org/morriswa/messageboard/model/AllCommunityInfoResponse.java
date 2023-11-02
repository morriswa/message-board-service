package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.morriswa.messageboard.entity.Community;

import java.util.GregorianCalendar;
import java.util.UUID;

@Data @AllArgsConstructor
public class AllCommunityInfoResponse {
    private Long communityId;
    private String communityLocator;
    private String displayName;
    private UUID ownerId;
    private GregorianCalendar dateCreated;
    private AllCommunityResourceURLs resourceUrls;
    private int communityMemberCount;

    public AllCommunityInfoResponse(Community community,
                                    int communityMemberCount,
                                    AllCommunityResourceURLs resources) {
        this.communityId = community.getCommunityId();
        this.communityLocator = community.getCommunityLocator();
        this.displayName = community.getCommunityDisplayName();
        this.ownerId = community.getCommunityOwnerUserId();
        this.dateCreated = community.getDateCreated();
        this.resourceUrls = resources;
        this.communityMemberCount = communityMemberCount;
    }
}
