package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Resource;

import java.util.Optional;
import java.util.UUID;

public interface ResourceDao {
    Optional<Resource> findResourceByResourceId(UUID resourceId);

    void createNewPostResource(Resource newResource);
}
