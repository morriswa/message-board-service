package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Community;

import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    Optional<Community> findCommunityByCommunityLocator(String communityLocator);

    Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId);

    Optional<Community> findCommunityByCommunityId(Long communityId);

    boolean existsByCommunityLocator(String communityLocator);

    void createNewCommunity(Community newCommunity);

    void updateCommunityAttributes(Community community);
}
