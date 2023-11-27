package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.model.CommunityMembership;
import org.morriswa.messageboard.model.CommunityMember;
import org.morriswa.messageboard.validation.request.JoinCommunityRequest;

import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberDao {

    @Deprecated Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId);

    @Deprecated int countCommunityMembersByCommunityId(Long communityId);

    void createNewRelationship(@Valid JoinCommunityRequest newRelationship);

    boolean relationshipExists(UUID userId, Long communityId);

    void deleteRelationship(UUID userId, Long communityId);

    CommunityMembership retrieveRelationship(UUID userId, Long communityId);

    void updateCommunityMemberModerationLevel(UUID userId, Long communityId, ModerationLevel level);
}
