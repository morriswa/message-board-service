package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.entity.Community;
import org.morriswa.messageboard.model.validatedrequest.CreateCommunityRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    Optional<Community> findCommunity(String communityDisplayName);

    Optional<Community> findCommunity(Long communityId);

    List<Community> findAllCommunities(UUID userId);

    void createNewCommunity(CreateCommunityRequest newCommunity);

    void setCommunityLocator(Long communityId, String ref);

    void setCommunityDisplayName(Long communityId, String displayName);

    boolean existsByCommunityLocator(String communityLocator);

    boolean verifyUserCanPostInCommunity(UUID userId, Long communityId);

    boolean verifyUserCanEditCommunity(UUID userId, Long communityId);
}
