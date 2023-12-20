package org.morriswa.messageboard.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.morriswa.messageboard.enumerated.PostContentType;

import java.net.URL;
import java.util.List;
import java.util.UUID;


public record PostDraft(
        UUID draftId, UUID userId, Long communityId,
        UUID resourceId, String caption, String description
) {
    public record Response(
        @JsonUnwrapped PostDraft draft,
        PostContentType contentType,
        List<URL> resources
    ) { }
}
