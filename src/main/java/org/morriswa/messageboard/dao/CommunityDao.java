package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.validation.request.CreateCommunityRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    List<Community> searchForCommunities(String searchText);

    Optional<Community> findCommunity(String communityDisplayName);

    Optional<Community> findCommunity(Long communityId);

    List<Community> findAllCommunities(UUID userId);

    void createNewCommunity(CreateCommunityRequest newCommunity) throws ValidationException;

    void updateCommunityAttrs(Long communityId, @Valid UpdateCommunityRequest attributesToUpdate) throws ValidationException;

    @Deprecated void setCommunityLocator(Long communityId, String ref);

    @Deprecated void setCommunityDisplayName(Long communityId, String displayName);

    @Deprecated boolean verifyUserCanPostInCommunity(UUID userId, Long communityId);

}
