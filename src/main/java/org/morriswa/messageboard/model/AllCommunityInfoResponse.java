package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.GregorianCalendar;
import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor @Data
public class AllCommunityInfoResponse {
    private Long communityId;
    private String displayName;
    private UUID ownerId;
    private GregorianCalendar dateCreated;
    private AllCommunityResourceURLs resourceUrls;
}
