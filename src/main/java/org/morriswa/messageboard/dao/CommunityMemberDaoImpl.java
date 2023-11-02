package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.entity.CommunityMember;
import org.morriswa.messageboard.model.AllCommunityInfoResponse;
import org.morriswa.messageboard.model.CommunityStanding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

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
    public List<AllCommunityInfoResponse> findAllByUserId(UUID userId) {
        final String query =
        """
            select
                cm.id relationshipId,
                cm.community_id communityId,
                cm.user_id userId,
                co.community_ref communityLocator,
                co.display_name displayName,
                co.owner owner,
                co.date_created dateCreated
            from community_member cm
            full outer join community co
              on co.id=cm.community_id
            where (co.owner=:userId or cm.user_id=:userId)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
        }};

        try {
            return jdbc.query(query, params, rs -> {
                List<AllCommunityInfoResponse> response = new ArrayList<>();

                while (rs.next()) {
                    var communityId = rs.getLong("communityId");

                    int communityMembers = countCommunityMembersByCommunityId(communityId);

                    var buildingCommunityResponse = new AllCommunityInfoResponse(
                            communityId,
                            rs.getString("communityLocator"),
                            rs.getString("displayName"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("dateCreated")),
                            null,
                            communityMembers);

                    response.add(buildingCommunityResponse);
                }

                return response;
            });
        } catch (Exception e) {
            log.error("Exception occurred CommunityMemberDao.findAllByUserId", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createNewRelationship(@Valid CommunityMember newRelationship) {
        final String query =
            """
                insert into community_member(id, community_id, user_id, moderation_level, standing, date_updated, date_created)
                values(DEFAULT, :communityId, :userId, :moderationLevel, :standing, current_timestamp, current_timestamp)
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", newRelationship.getCommunityId());
            put("userId", newRelationship.getUserId());
            put("moderationLevel", newRelationship.getModerationLevel());
            put("standing", newRelationship.getCommunityStanding().toString());
        }};

        jdbc.update(query, params);
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
}
