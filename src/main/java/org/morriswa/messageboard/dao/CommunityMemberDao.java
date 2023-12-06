package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.model.CommunityMember;
import org.morriswa.messageboard.model.CommunityWatcherStatus;
import org.morriswa.messageboard.validation.request.JoinCommunityRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberDao {

    Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId);

    @Deprecated int countCommunityMembersByCommunityId(Long communityId);

    void createNewRelationship(@Valid JoinCommunityRequest newRelationship);

    boolean relationshipExists(UUID userId, Long communityId);

    void deleteRelationship(UUID userId, Long communityId);

    CommunityWatcherStatus getWatcherStatus(UUID userId, Long communityId);

    void updateCommunityMemberModerationLevel(UUID userId, Long communityId, ModerationLevel level);

    List<CommunityMember> getCommunityModerators(Long communityId);
}
