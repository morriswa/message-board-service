package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.morriswa.messageboard.model.entity.Post;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Getter
public class PostUserResponse extends PostResponse{


    private final UserInfo userInfo;

    @Data @AllArgsConstructor
    public static class UserInfo {
        private final UUID userId;
        private final String displayName;
        private final URL userProfileImage;
    }

    public PostUserResponse(Post post, UserProfile user, List<URL> resources) {
        super(post.getPostId(), post.getVote(), post.getCaption(), post.getDescription(),
                post.getContentType(), post.getDateCreated(), resources);
        this.userInfo = new UserInfo(user.getUserId(), user.getDisplayName(), user.getUserProfileImage());
    }
}
