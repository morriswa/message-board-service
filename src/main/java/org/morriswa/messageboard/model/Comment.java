package org.morriswa.messageboard.model;

import java.util.GregorianCalendar;
import java.util.UUID;

public record Comment(
        Long commentId, UUID userId, String displayName,
        Long postId, Long parentId, String body,
        int vote, GregorianCalendar dateCreated
) { }
