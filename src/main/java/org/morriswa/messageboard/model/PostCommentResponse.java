package org.morriswa.messageboard.model;

import lombok.Getter;

import java.net.URL;
import java.util.List;

@Getter
public class PostCommentResponse extends PostResponse {
    private final List<Comment> comments;

    public PostCommentResponse(Post post, List<URL> resources, List<Comment> comments) {
        super(post, resources);
        this.comments = comments;
    }
}
