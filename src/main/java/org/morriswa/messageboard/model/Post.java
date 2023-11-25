package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.util.GregorianCalendar;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Post {
    private final Long postId;
    private final UUID userId;
    private final String displayName;
    private final Long communityId;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final GregorianCalendar dateCreated;
    @JsonIgnore private final UUID resourceId;
    private final int vote;
}
