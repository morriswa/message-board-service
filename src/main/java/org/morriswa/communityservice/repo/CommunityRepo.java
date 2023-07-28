package org.morriswa.communityservice.repo;

import org.morriswa.communityservice.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepo extends JpaRepository<Community, Long> {
    Optional<Community> findCommunityByCommunityDisplayName(String communityDisplayName);
    Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId);
}
