package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepo extends JpaRepository<Community, Long> {

    Optional<Community> findCommunityByCommunityLocator(String communityLocator);

    Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId);

    Optional<Community> findCommunityByCommunityId(Long communityId);

    boolean existsByCommunityLocator(String communityLocator);
}
