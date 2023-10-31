package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.CommunityMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CommunityMemberDaoImpl implements CommunityMemberDao{

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public CommunityMemberDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId) {
        return Optional.empty();
    }

    @Override
    public int countCommunityMembersByCommunityId(Long communityId) {
        return 0;
    }

    @Override
    public List<CommunityMember> findAllByUserId(UUID userId) {
        return null;
    }

    @Override
    public void createNewRelationship(CommunityMember newRelationship) {

    }

    @Override
    public void deleteRelationship(Long communityId) {

    }
}
