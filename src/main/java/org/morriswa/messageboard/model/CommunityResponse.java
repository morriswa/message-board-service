package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.morriswa.messageboard.model.Community;

import java.net.URL;

@Getter
public class CommunityResponse extends Community {
    private final AllCommunityResourceURLs resourceUrls;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AllCommunityResourceURLs {
        private URL icon;
        private URL banner;
    }

    public CommunityResponse(Community community, AllCommunityResourceURLs resourceUrls) {
        super(community.getCommunityId(), community.getCommunityLocator(), community.getDisplayName(),
                community.getOwnerId(), community.getDateCreated(), community.getCommunityMemberCount());
        this.resourceUrls = resourceUrls;
    }
}
