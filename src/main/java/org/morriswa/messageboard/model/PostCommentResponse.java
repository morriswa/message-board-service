package org.morriswa.messageboard.model;

import lombok.Getter;

import java.net.URL;
import java.util.List;

@Getter
public class PostCommentResponse extends PostUserResponse {
    private final List<Comment> comments;

    public PostCommentResponse(Post post, UserProfileResponse user, List<URL> resources, List<Comment> comments) {
        super(post, user, resources);
        this.comments = comments;
    }
}
