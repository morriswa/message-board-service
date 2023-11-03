package org.morriswa.messageboard.model.responsebody;

import lombok.Getter;
import org.morriswa.messageboard.model.entity.User;

import java.net.URL;

/**
 * Class containing the entirety of the user's profile, including resources
 */
@Getter
public class UserProfile extends User {
    private final URL userProfileImage;

    public UserProfile(User user, URL userProfileImage) {
        super(user.getUserId(), user.getAuthZeroId(), user.getEmail(), user.getDisplayName(), user.getRole());
        this.userProfileImage = userProfileImage;
    }
}
