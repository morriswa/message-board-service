package org.morriswa.communityservice.repo;

import org.morriswa.communityservice.entity.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityMemberRepo extends JpaRepository<CommunityMember, Long> {
    Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId);
}
