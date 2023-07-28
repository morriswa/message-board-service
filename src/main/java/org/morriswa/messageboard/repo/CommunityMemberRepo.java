package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityMemberRepo extends JpaRepository<org.morriswa.messageboard.entity.CommunityMember, Long> {
    Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId);
}
