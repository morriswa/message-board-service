package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.CommunityMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberDao {

    Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId);

    int countCommunityMembersByCommunityId(Long communityId);

    List<CommunityMember> findAllByUserId(UUID userId);

    void createNewRelationship(CommunityMember newRelationship);

    void deleteRelationship(Long communityId);
}
