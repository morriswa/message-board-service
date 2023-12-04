package org.morriswa.messageboard.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

public record PostWithCommunityInfoRow(
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
