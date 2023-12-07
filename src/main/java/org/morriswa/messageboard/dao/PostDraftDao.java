package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.control.requestbody.DraftBody;
import org.morriswa.messageboard.model.PostDraft;

import java.util.Optional;
import java.util.UUID;

public interface PostDraftDao {

    void create(UUID id, UUID userId, Long communityId, UUID resourceId, DraftBody draft);

    Optional<PostDraft> getDraft(UUID draftId);

    void edit(UUID userId, UUID draftId, DraftBody draft);

    void clearUsersDrafts(UUID userId);
}
