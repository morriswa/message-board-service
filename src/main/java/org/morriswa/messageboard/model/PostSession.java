package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor @NoArgsConstructor
@Getter
public class PostSession {
    private UUID sessionId;
    private UUID userId;
    private Long communityId;
    private UUID resourceId;
    private String caption;
    private String description;
}
