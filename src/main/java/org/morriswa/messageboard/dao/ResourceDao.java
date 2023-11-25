package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.Resource;

import java.util.Optional;
import java.util.UUID;

public interface ResourceDao {
    Optional<Resource> findResourceByResourceId(UUID resourceId) throws ResourceException;

    void createNewPostResource(Resource newResource) throws ResourceException;

    void updateResource(Resource resource) throws ResourceException;
}
