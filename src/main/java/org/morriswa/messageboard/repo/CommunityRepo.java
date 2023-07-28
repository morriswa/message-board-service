package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepo extends JpaRepository<org.morriswa.messageboard.entity.Community, Long> {
    Optional<org.morriswa.messageboard.entity.Community> findCommunityByCommunityDisplayName(String communityDisplayName);
    Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId);
}
