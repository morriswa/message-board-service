package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PostSession {
    private final UUID sessionId;
    private final UUID userId;
    private final Long communityId;
    private final UUID resourceId;
    private final String caption;
    private final String description;
}
