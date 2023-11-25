package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.PostDraft;

import java.util.Optional;
import java.util.UUID;

public interface PostDraftDao {

    void create(UUID id, UUID userId, Long communityId, UUID resourceId, Optional<String> caption, Optional<String> description);

    Optional<PostDraft> getDraft(UUID draftId);

    void edit(UUID userId, UUID draftId, Optional<String> caption, Optional<String> description);

    void clearUsersDrafts(UUID userId);
}
