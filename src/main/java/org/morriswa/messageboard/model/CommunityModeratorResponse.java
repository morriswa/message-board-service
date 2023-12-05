package org.morriswa.messageboard.model;


import lombok.Getter;
import lombok.Setter;
import org.morriswa.messageboard.enumerated.ModerationLevel;

import java.net.URL;
import java.util.UUID;

@Getter
public class CommunityModeratorResponse {
    private final UUID userId;
    private final String displayName;
    private final String email;
    private final ModerationLevel moderationLevel;

    @Setter
    private URL profileImage;

    public CommunityModeratorResponse(UUID userId, String displayName, String email, ModerationLevel moderationLevel) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.moderationLevel = moderationLevel;
        this.profileImage = null;
    }
}
