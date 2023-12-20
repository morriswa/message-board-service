package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.CreateCommunityRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    List<Community> searchForCommunities(String searchText);

    Optional<Community> findCommunity(String communityDisplayName);

    Optional<Community> findCommunity(Long communityId);

    List<Community> findAllCommunities(UUID userId);

    void updateCommunityAttrs(Long communityId, @Valid UpdateCommunityRequest attributesToUpdate) throws ValidationException;

    void createNewCommunity(UUID userId, @Valid CreateCommunityRequest newCommunity) throws ValidationException;
}
