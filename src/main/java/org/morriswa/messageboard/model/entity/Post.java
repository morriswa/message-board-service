package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Post {
    private final Long postId;
    private final UUID userId;
    private final Long communityId;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final GregorianCalendar dateCreated;
    private final UUID resourceId;
    private final int vote;
}
