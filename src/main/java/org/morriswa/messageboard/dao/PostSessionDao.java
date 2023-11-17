package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.PostSession;

import java.util.Optional;
import java.util.UUID;

public interface PostSessionDao {

    void create(UUID id, UUID userId, Long communityId, UUID resourceId, Optional<String> caption, Optional<String> description);

    PostSession getSession(UUID sessionToken);
}
