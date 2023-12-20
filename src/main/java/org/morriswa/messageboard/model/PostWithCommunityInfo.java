package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

public record PostWithCommunityInfo (
        Long postId,
        UUID userId,
        String displayName,
        Long communityId,
        String communityLocator,
        String communityDisplayName,
        String caption,
        String description,
        PostContentType contentType,
        GregorianCalendar dateCreated,
        @JsonIgnore UUID resourceId,
        int vote
) {
}