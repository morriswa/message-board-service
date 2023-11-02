package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.NewCommunityRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    Optional<Community> getAllCommunityInfo(String communityDisplayName);

    Optional<Community> getAllCommunityInfo(Long communityId);

    List<Community> findAllCommunitiesByUserId(UUID userId);

    void createNewCommunity(NewCommunityRequest newCommunity);

    void setCommunityLocator(Long communityId, String ref);

    void setCommunityDisplayName(Long communityId, String displayName);

    boolean existsByCommunityLocator(String communityLocator);

    boolean verifyUserCanPostInCommunity(UUID userId, Long communityId);

    boolean verifyUserCanEditCommunity(UUID userId, Long communityId);
}
