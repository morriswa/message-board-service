package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.UUID;

@Data @AllArgsConstructor
public class UserProfile {
    private final UUID userId;
    private final String displayName;
    private final String authZeroId;
    private final UserRole role;
    private final String email;
    private URL userProfileImage;

    public UserProfile(UUID userId, String authZeroId, String email, String displayName, UserRole role) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.authZeroId = authZeroId;
        this.role = role;
        this.userProfileImage = null;
    }
}
