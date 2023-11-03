package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

@Data @AllArgsConstructor
public class Post {
    private final Long postId;
    private final UUID userId;
    private final Long communityId;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final GregorianCalendar dateCreated;
    private final UUID resourceId;
}
