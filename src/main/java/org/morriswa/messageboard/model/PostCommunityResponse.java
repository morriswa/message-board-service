package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.morriswa.messageboard.dao.model.PostWithCommunityInfoRow;

import java.net.URL;
import java.util.ArrayList;

@Getter
public class PostCommunityResponse extends PostResponse {

    private final CommunityInfo communityInfo;

    public PostCommunityResponse(PostWithCommunityInfoRow post, ArrayList<URL> resourceUrls, URL communityResourceURL) {
        super(post, resourceUrls);
        this.communityInfo = new CommunityInfo(post.communityLocator(), post.communityDisplayName(), communityResourceURL);
    }

    @Data @AllArgsConstructor
    public static class CommunityInfo {
        private final String communityLocator;
        private final String displayName;
        private final URL icon;
    }

    public PostCommunityResponse(Post post, CommunityResponse community, ArrayList<URL> resourceUrls) {
        super(post, resourceUrls);
        this.communityInfo = new CommunityInfo(
                community.getCommunityLocator(),
                community.getDisplayName(),
                community.getResourceUrls().getIcon());
    }
}
