package org.morriswa.messageboard.model;

import lombok.Getter;
import org.morriswa.messageboard.model.User;

import java.net.URL;

/**
 * Class containing the entirety of the user's profile, including resources
 */
@Getter
public class UserProfileResponse extends User {
    private final URL userProfileImage;

    public UserProfileResponse(User user, URL userProfileImage) {
        super(user.getUserId(), user.getAuthZeroId(), user.getEmail(), user.getDisplayName(), user.getRole());
        this.userProfileImage = userProfileImage;
    }
}
