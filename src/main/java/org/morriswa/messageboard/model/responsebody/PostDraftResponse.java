package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.PostContentType;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor @Getter
public class PostDraftResponse {
    private final UUID draftId;
    private final UUID userId;
    private final Long communityId;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final List<URL> resources;
}
