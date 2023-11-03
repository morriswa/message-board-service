package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

@Data
public class PostResponse {

    private final Long postId;
    private final UserInfo userInfo;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final GregorianCalendar dateCreated;
    private final List<URL> resources;

    @Data @AllArgsConstructor
    public static class UserInfo {
        private final UUID userId;
        private final String displayName;
        private final URL userProfileImage;
    }


    public PostResponse(Post post, UserProfile user, List<URL> resources) {
        this.postId = post.getPostId();
        this.userInfo = new UserInfo(user.getUserId(), user.getDisplayName(), user.getUserProfileImage());
        this.caption = post.getCaption();
        this.description = post.getDescription();
        this.contentType = post.getContentType();
        this.dateCreated = post.getDateCreated();
        this.resources = resources;
    }
}
