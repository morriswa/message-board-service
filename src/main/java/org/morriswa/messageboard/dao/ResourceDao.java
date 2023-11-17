package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.entity.Resource;

import java.util.Optional;
import java.util.UUID;

public interface ResourceDao {
    Optional<Resource> findResourceByResourceId(UUID resourceId) throws ResourceException;

    void createNewPostResource(Resource newResource) throws ResourceException;

    void updateResource(Resource resource) throws ResourceException;
}
