package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;

@Getter
public class PostCommunityResponse extends PostResponse {

    private final CommunityInfo communityInfo;

    @Data @AllArgsConstructor
    public static class CommunityInfo {
        private final Long communityId;
        private final String communityLocator;
        private final String displayName;
        private final URL icon;
    }

    public PostCommunityResponse(Post post, CommunityResponse community, ArrayList<URL> resourceUrls) {
        super(post.getPostId(), post.getVote(), post.getCaption(), post.getDescription(),
                post.getContentType(), post.getDateCreated(), resourceUrls);
        this.communityInfo = new CommunityInfo(
                community.getCommunityId(),
                community.getCommunityLocator(),
                community.getDisplayName(),
                community.getResourceUrls().getIcon());
    }
}
