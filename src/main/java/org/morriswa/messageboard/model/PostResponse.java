package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.morriswa.messageboard.entity.Post;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor
public class PostResponse {

    private UserInfo userInfo;
    private String caption;
    private String description;
    private List<URL> resources;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class UserInfo {
        private UUID userId;
        private String displayName;
        private URL userProfileImage;
    }


    public PostResponse(Post post, UserProfileResponse user, List<URL> resources) {
        this.userInfo = new UserInfo(user.getUserId(), user.getDisplayName(), user.getUserProfileImage());
        this.caption = post.getCaption();
        this.description = post.getDescription();
        this.resources = resources;
    }
}
