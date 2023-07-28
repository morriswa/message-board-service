package org.morriswa.userprofileservice.model;

import lombok.Getter;
import org.morriswa.userprofileservice.entity.User;

import java.net.URL;
import java.util.UUID;

@Getter
public class UserProfileResponse {
    private final UUID userId;
    private final String displayName;
    private final String email;
    private final URL userProfileImage;

    public UserProfileResponse(User userEntity, URL userProfileImage) {
        this.userId = userEntity.getUserId();
        this.displayName = userEntity.getDisplayName();
        this.email = userEntity.getEmail();
        this.userProfileImage = userProfileImage;
    }
}
