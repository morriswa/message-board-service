package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;

@Data @AllArgsConstructor
public class Community {
    private Long communityId;
    private String communityLocator;
    private String displayName;
    private UUID ownerId;
    private GregorianCalendar dateCreated;
    private AllCommunityResourceURLs resourceUrls;
    private int communityMemberCount;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AllCommunityResourceURLs {
        private URL icon;
        private URL banner;
    }
}
