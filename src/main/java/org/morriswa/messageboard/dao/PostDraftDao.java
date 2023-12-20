package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.DraftBody;
import org.morriswa.messageboard.model.PostDraft;

import java.util.Optional;
import java.util.UUID;

public interface PostDraftDao {

    void create(UUID id, UUID userId, Long communityId, UUID resourceId, @Valid DraftBody draft);

    Optional<PostDraft> getDraft(UUID draftId);

    void edit(UUID userId, UUID draftId, @Valid DraftBody draft);

    void clearUsersDrafts(UUID userId);
}
