package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;


public record Post(
    Long postId, UUID userId, String displayName,
    Long communityId, String caption, String description,
    PostContentType contentType, GregorianCalendar dateCreated,
    @JsonIgnore UUID resourceId, int vote
) {
    public record Response (
        @JsonUnwrapped Post post,
        List<URL> resources
    ) {
        public Response(PostWithCommunityInfo post, List<URL> resources) {
            this(new Post(
                    post.postId(), post.userId(), post.displayName(),
                    post.communityId(), post.caption(), post.description(),
                    post.contentType(), post.dateCreated(), post.resourceId(), post.vote()
                ),
                resources)
            ;
        }
    }

    public record PostCommentResponse (
        @JsonUnwrapped Response post,
        List<Comment> comments
    ) {
        public PostCommentResponse(Post post, List<URL> resources, List<Comment> comments) {
            this(new Response(post, resources), comments);
        }
    }

    public record PostCommunityResponse (
        @JsonUnwrapped Response postResponse,
        CommunityInfo communityInfo
    ) {
        public PostCommunityResponse(PostWithCommunityInfo post, ArrayList<URL> resourceUrls, URL communityResourceURL) {
            this(
                new Response(post, resourceUrls),
                new CommunityInfo(post.communityLocator(), post.communityDisplayName(), communityResourceURL)
            );
        }

        private record CommunityInfo(String communityLocator, String displayName, URL icon) { }
    }
}
