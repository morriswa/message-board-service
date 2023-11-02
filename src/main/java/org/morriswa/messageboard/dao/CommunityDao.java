package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Community;
import org.morriswa.messageboard.model.AllCommunityInfoResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityDao {

    Optional<Community> findCommunityByCommunityLocator(String communityLocator);

    Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId);

    Optional<Community> findCommunityByCommunityId(Long communityId);

    List<AllCommunityInfoResponse> findAllCommunitiesByUserId(UUID userId);

    boolean existsByCommunityLocator(String communityLocator);

    void createNewCommunity(Community newCommunity);

    void setCommunityLocator(Long communityId, String ref);

    void setCommunityDisplayName(Long communityId, String displayName);

    Optional<AllCommunityInfoResponse> getAllCommunityInfoByCommunityLocator(String communityDisplayName);

    Optional<AllCommunityInfoResponse> getAllCommunityInfoByCommunityId(Long communityId);
}
