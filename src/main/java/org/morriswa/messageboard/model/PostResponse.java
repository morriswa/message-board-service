package org.morriswa.messageboard.model;

import lombok.Getter;
import org.morriswa.messageboard.dao.model.PostWithCommunityInfoRow;

import java.net.URL;
import java.util.List;

@Getter
public class PostResponse extends Post{
    private final List<URL> resources;

    public PostResponse(Post post,
                        List<URL> resources) {
        super(post.getPostId(), post.getUserId(), post.getDisplayName(),
                post.getCommunityId(), post.getCaption(), post.getDescription(),
                post.getContentType(), post.getDateCreated(), post.getResourceId(), post.getVote());
        this.resources = resources;
    }

    public PostResponse(PostWithCommunityInfoRow post,
                        List<URL> resources) {
        super(post.postId(), post.userId(), post.displayName(),
                post.communityId(), post.caption(), post.description(),
                post.contentType(), post.dateCreated(), post.resourceId(), post.vote());
        this.resources = resources;
    }
}
