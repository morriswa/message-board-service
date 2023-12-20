package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.morriswa.messageboard.enumerated.UserRole;

import java.net.URL;
import java.util.UUID;


public record User(UUID userId, @JsonIgnore String authZeroId, String email, String displayName, UserRole role) {
    /**
     * Class containing the entirety of the user's profile, including resources
     */
    public record Response(
            @JsonUnwrapped User user,
            URL userProfileImage
    ) { }
}
