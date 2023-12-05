package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.model.CommunityMembership;
import org.morriswa.messageboard.model.CommunityMember;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.model.CommunityModeratorResponse;
import org.morriswa.messageboard.validation.request.JoinCommunityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.timestampToGregorian;

@Component @Slf4j
public class CommunityMemberDaoImpl implements CommunityMemberDao{

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public CommunityMemberDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<CommunityMember> findCommunityMemberByUserIdAndCommunityId(UUID userId, Long communityId) {
        final String query = "select * from community_member where user_id=:userId and community_id=:communityId";

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
            put("userId", userId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                return Optional.of(
                        new CommunityMember(
                                rs.getLong("id"),
                                rs.getLong("community_id"),
                                rs.getObject("user_id", UUID.class),
                                rs.getInt("moderation_level"),
                                CommunityStanding.valueOf(rs.getString("standing")),
                                timestampToGregorian(rs.getTimestamp("date_updated")),
                                timestampToGregorian(rs.getTimestamp("date_created"))));
            }

            return Optional.empty();
        });
    }

    @Override
    public int countCommunityMembersByCommunityId(Long communityId) {
        final String query = "select count(id) from community_member where community_id=:communityId";

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
        }};

        return jdbc.query(query, params, rs->{
            if (rs.next()) return rs.getInt("count");
            return 0;
        });
    }

    @Override
    public void createNewRelationship(@Valid JoinCommunityRequest newRelationship) {
        final String query =
            """
                insert into community_member(id, community_id, user_id, moderation_level, standing, date_updated, date_created)
                values(DEFAULT, :communityId, :userId, :moderationLevel, :standing, current_timestamp, current_timestamp)
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", newRelationship.getCommunityId());
            put("userId", newRelationship.getUserId());
            put("moderationLevel", newRelationship.getModerationLevel().toString());
            put("standing", newRelationship.getCommunityStanding().toString());
        }};

        jdbc.update(query, params);
    }

    @Override
    public boolean relationshipExists(UUID userId, Long communityId) {
        final String query = "select 1 from community_member where community_id=:communityId and user_id=:userId";

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
            put("userId", userId);
        }};

        return jdbc.query(query, params, ResultSet::next);
    }

    @Override
    public void deleteRelationship(UUID userId, Long communityId) {
        final String query = "delete from community_member where user_id=:userId and community_id=:communityId";

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
            put("userId", userId);
        }};

        jdbc.update(query, params);
    }

    @Override
    public CommunityMembership retrieveRelationship(UUID userId, Long communityId) {
        final String query = """
            select *
            from community_member
            where community_id=:communityId and user_id=:userId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
            put("userId", userId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                return new CommunityMembership(
                        true,
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        CommunityStanding.valueOf(rs.getString("standing")),
                        ModerationLevel.valueOf(rs.getString("moderation_level"))
                );
            }

            return new CommunityMembership(false, userId, communityId, null, null);
        });
    }

    @Override
    public void updateCommunityMemberModerationLevel(UUID userId, Long communityId, ModerationLevel level) {
        final String query = """
            update community_member
                set moderation_level = :level
            where user_id = :userId and community_id = :communityId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
            put("userId", userId);
            put("level", level.toString());
        }};

        jdbc.update(query, params);
    }

    @Override
    public List<CommunityModeratorResponse> getCommunityModerators(Long communityId) {
        final String query = """
            select
                up.id user_id,
                up.display_name display_name,
                up.email email,
                cm.moderation_level moderation_level
            from community_member cm
                join user_profile up
                on cm.user_id=up.id
            where community_id=:communityId
                and moderation_level != 'NONE'
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
        }};

        return jdbc.query(query, params, rs -> {
            List<CommunityModeratorResponse> response = new ArrayList<>();

            while (rs.next()) {
                response.add(new CommunityModeratorResponse(
                        rs.getObject("user_id", UUID.class),
                        rs.getString("display_name"),
                        rs.getString("email"),
                        ModerationLevel.valueOf(rs.getString("moderation_level"))
                ));
            }

            return response;
        });
    }
}
