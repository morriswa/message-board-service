package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.morriswa.messageboard.model.entity.Post;
import org.morriswa.messageboard.model.enumerated.PostContentType;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

@Getter
public class PostResponse {

    private final Long postId;
    private final UserInfo userInfo;
    private final int vote;
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
        this.vote = post.getVote();
        this.userInfo = new UserInfo(user.getUserId(), user.getDisplayName(), user.getUserProfileImage());
        this.caption = post.getCaption();
        this.description = post.getDescription();
        this.contentType = post.getContentType();
        this.dateCreated = post.getDateCreated();
        this.resources = resources;
    }
}
