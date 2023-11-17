package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.model.entity.Resource;

import java.util.Optional;
import java.util.UUID;

public interface ResourceDao {
    Optional<Resource> findResourceByResourceId(UUID resourceId);

    void createNewPostResource(Resource newResource) throws JsonProcessingException;

    void updateResource(Resource resource) throws JsonProcessingException;
}
