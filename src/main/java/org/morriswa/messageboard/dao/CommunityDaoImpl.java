package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.validatedrequest.CreateCommunityRequest;
import org.morriswa.messageboard.model.entity.Community;
import org.morriswa.messageboard.model.enumerated.CommunityStanding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.timestampToGregorian;

@Component @Slf4j
public class CommunityDaoImpl implements CommunityDao {

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    CommunityDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    private Optional<Community> unwrapCommunityResultSet(ResultSet rs) throws SQLException {
        if (rs.next())
            return Optional.of(new Community(
                    rs.getLong("communityId"),
                    rs.getString("communityLocator"),
                    rs.getString("displayName"),
                    rs.getObject("owner", UUID.class),
                    timestampToGregorian(rs.getTimestamp("dateCreated")),
                    rs.getInt("count")));

        return Optional.empty();
    }

    @Override
    public List<Community> findAllCommunities(UUID userId) {
        final String query = """            
            select DISTINCT
                    co.id communityId,
                    co.community_ref communityLocator,
                    co.display_name displayName,
                    co.owner owner,
                    co.date_created dateCreated,
                    (select count(cme.id) from community_member cme where co.id=cme.community_id) AS count
                from community_member cm
                full join community co
                    on co.id=cm.community_id
                where cm.user_id=:userId or co.owner=:userId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
        }};

        try {
            return jdbc.query(query, params, rs -> {
                List<Community> response = new ArrayList<>();

                while (rs.next())
                    response.add(new Community(
                            rs.getLong("communityId"),
                            rs.getString("communityLocator"),
                            rs.getString("displayName"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("dateCreated")),
                            rs.getInt("count")));

                return response;
            });
        } catch (Exception e) {
            log.error("Exception occurred CommunityDao.findAllByUserId", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsByCommunityLocator(String communityLocator) {
        final String query = "select 1 from community where community_ref=:communityLocator";

        Map<String, Object> params = new HashMap<>(){{
            put("communityLocator", communityLocator);
        }};

        return jdbc.query(query, params, ResultSet::next);
    }

    @Override
    public void createNewCommunity(CreateCommunityRequest newCreateCommunityRequest) {
        final String query = """
            insert into community(id, community_ref, display_name, owner, date_created)
            values (DEFAULT, :communityRef, :displayName, :owner, current_timestamp)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityRef", newCreateCommunityRequest.getCommunityLocator());
            put("displayName", newCreateCommunityRequest.getCommunityDisplayName());
            put("owner", newCreateCommunityRequest.getCommunityOwnerUserId());
        }};


        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }
    }

    @Override
    public void setCommunityLocator(Long communityId, String communityLocator) {
        final String query = """
            update community
                set community_ref = :communityLocator
            where id = :id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", communityId);
            put("communityLocator",communityLocator);
        }};

        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }
    }

    @Override
    public void setCommunityDisplayName(Long communityId, String displayName) {
        final String query = """
            update community
                set display_name = :displayName
            where id = :id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", communityId);
            put("displayName",displayName);
        }};

        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }
    }

    @Override
    public boolean verifyUserCanPostInCommunity(UUID userId, Long communityId) {
        final String query = """            
            select DISTINCT
                    co.owner ownerId,
                    cm.user_id userId,
                    cm.standing standing
                from community_member cm
                full join community co
                    on co.id=cm.community_id
                where (cm.user_id=:userId or co.owner=:userId) and co.id=:communityId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("communityId", communityId);
        }};

        try {
            return Boolean.TRUE.equals(jdbc.query(query, params, rs -> {
                if (rs.next()) {
                    var owner = rs.getObject("ownerId", UUID.class);
                    if (owner.equals(userId)) return true;

                    var user = rs.getObject("userId", UUID.class);
                    var standing = CommunityStanding.valueOf(rs.getString("standing"));

                    if (user.equals(userId) && standing.equals(CommunityStanding.HEALTHY)) return true;
                }

                return false;
            }));
        } catch (Exception e) {
            log.error("Exception occurred CommunityDao.verifyUserCanPostInCommunity", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyUserCanEditCommunity(UUID userId, Long communityId) {
        final String query = """            
            select owner
            from community
            where owner=:userId and id=:communityId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("communityId", communityId);
        }};

        try {
            return Boolean.TRUE.equals(jdbc.query(query, params, rs -> {
                if (rs.next()) {
                    var owner = rs.getObject("owner", UUID.class);
                    return owner.equals(userId);
                }

                return false;
            }));
        } catch (Exception e) {
            log.error("Exception occurred CommunityDao.verifyUserCanEditCommunity", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Community> findCommunity(String communityLocator) {
        final String query = """            
            select
                co.id communityId,
                co.community_ref communityLocator,
                co.display_name displayName,
                co.owner owner,
                co.date_created dateCreated,
                (select count(cme.id) from community_member cme where co.id=cme.community_id) AS count
            from community co
            where co.community_ref=:communityLocator;
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityLocator", communityLocator);
        }};

        try {
            return jdbc.query(query, params, this::unwrapCommunityResultSet);
        } catch (Exception e) {
            log.error("Exception occurred CommunityDao.findCommunity", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Community> findCommunity(Long communityId) {
        final String query = """            
            select
                co.id communityId,
                co.community_ref communityLocator,
                co.display_name displayName,
                co.owner owner,
                co.date_created dateCreated,
                (select count(cme.id) from community_member cme where co.id=cme.community_id) AS count
            from community co
            where co.id=:communityId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityId", communityId);
        }};

        try {
            return jdbc.query(query, params, this::unwrapCommunityResultSet);
        } catch (Exception e) {
            log.error("Exception occurred CommunityDao.findCommunity", e);
            throw new RuntimeException(e);
        }
    }
}
