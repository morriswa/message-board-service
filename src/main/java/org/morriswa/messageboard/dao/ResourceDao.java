package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.Resource;

import java.util.Optional;
import java.util.UUID;

public interface ResourceDao {
    Optional<Resource> findResourceByResourceId(UUID resourceId) throws ResourceException;

    void createNewPostResource(@Valid Resource newResource) throws ResourceException;

    void updateResource(@Valid Resource resource) throws ResourceException;

    void deleteResource(UUID resourceId);
}
