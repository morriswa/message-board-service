package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;


public record Community(
        Long communityId, String communityLocator,
        String displayName, UUID ownerId,
        GregorianCalendar dateCreated, int communityMemberCount
) {
    public record Response(
            @JsonUnwrapped Community community,
            AllCommunityResourceURLs resourceUrls
    ) {
        public record AllCommunityResourceURLs (
                URL icon,
                URL banner
        ) { }
    }
}
