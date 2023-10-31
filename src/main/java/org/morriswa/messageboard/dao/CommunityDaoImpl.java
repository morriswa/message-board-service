package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Community;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CommunityDaoImpl implements CommunityDao {

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    CommunityDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Community> findCommunityByCommunityLocator(String communityLocator) {
        return Optional.empty();
    }

    @Override
    public Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId) {
        return Optional.empty();
    }

    @Override
    public Optional<Community> findCommunityByCommunityId(Long communityId) {
        return Optional.empty();
    }

    @Override
    public boolean existsByCommunityLocator(String communityLocator) {
        return false;
    }

    @Override
    public void createNewCommunity(Community newCommunity) {

    }

    @Override
    public void updateCommunityAttributes(Community community) {

    }
}
