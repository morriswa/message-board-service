package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.GregorianCalendar;
import java.util.UUID;

@AllArgsConstructor @Getter
public class Comment {
    private final Long commentId;
    private final UUID userId;
    private final Long postId;
    private final Long parentId;
    private final String body;
    private final int vote;
    private final GregorianCalendar dateCreated;
}
