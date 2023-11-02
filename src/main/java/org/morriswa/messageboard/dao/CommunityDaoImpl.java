package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.entity.Community;
import org.morriswa.messageboard.model.AllCommunityInfoResponse;
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
            return Optional.of(Community.builder()
                    .communityId(rs.getLong("id"))
                    .communityLocator(rs.getString("community_ref"))
                    .communityDisplayName(rs.getString("display_name"))
                    .communityOwnerUserId(rs.getObject("owner", UUID.class))
                    .dateCreated(timestampToGregorian(rs.getTimestamp("date_created")))
                    .build());

        return Optional.empty();
    }
    @Override
    public Optional<Community> findCommunityByCommunityLocator(String communityLocator) {
        final String query = "select id, community_ref, display_name, owner, date_created from community where community_ref=:communityLocator";

        Map<String, Object> params = new HashMap<>(){{
            put("communityLocator", communityLocator);
        }};

        return jdbc.query(query, params, this::unwrapCommunityResultSet);
    }

    @Override
    public Optional<Community> findCommunityByCommunityIdAndCommunityOwnerUserId(Long communityId, UUID communityOwnerUserId) {
        final String query =
            """
                select id, community_ref, display_name, owner, date_created from community
                where id=:id
                and owner=:owner
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", communityId);
            put("owner", communityOwnerUserId);
        }};

        return jdbc.query(query, params, this::unwrapCommunityResultSet);
    }

    @Override
    public Optional<Community> findCommunityByCommunityId(Long communityId) {
        final String query = "select id, community_ref, display_name, owner, date_created from community where id=:id";

        Map<String, Object> params = new HashMap<>(){{
            put("id", communityId);
        }};

        return jdbc.query(query, params, this::unwrapCommunityResultSet);
    }

    @Override
    public List<AllCommunityInfoResponse> findAllCommunitiesByUserId(UUID userId) {
        final String query =
            """            
                select
                    co.id communityId,
                    co.community_ref communityLocator,
                    co.display_name displayName,
                    co.owner owner,
                    co.date_created dateCreated,
                    (select count(cme.id) from community_member cme where co.id=cme.community_id) AS count
                from community co
                left join community_member cm
                on (co.id=cm.community_id and co.owner != cm.user_id)
                where co.owner=:userId or cm.user_id=:userId
            """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
        }};

        try {
            return jdbc.query(query, params, rs -> {
                List<AllCommunityInfoResponse> response = new ArrayList<>();

                while (rs.next())
                    response.add(new AllCommunityInfoResponse(
                            rs.getLong("communityId"),
                            rs.getString("communityLocator"),
                            rs.getString("displayName"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("dateCreated")),
                            null,
                            rs.getInt("count")));

                return response;
            });
        } catch (Exception e) {
            log.error("Exception occurred CommunityMemberDao.findAllByUserId", e);
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
    public void createNewCommunity(Community newCommunity) {
        final String query =
                """
                    insert into community(id, community_ref, display_name, owner, date_created)
                    values (DEFAULT, :communityRef, :displayName, :owner, :dateCreated)
                """;

        Map<String, Object> params = new HashMap<>(){{
            put("communityRef", newCommunity.getCommunityLocator());
            put("displayName", newCommunity.getCommunityDisplayName());
            put("owner", newCommunity.getCommunityOwnerUserId());
            put("dateCreated", newCommunity.getDateCreated());
        }};


        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }
    }

    @Override
    public void setCommunityLocator(Long communityId, String communityLocator) {
        final String query =
                """
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
        final String query =
                """
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
    public Optional<AllCommunityInfoResponse> getAllCommunityInfoByCommunityLocator(String communityLocator) {
        final String query =
                """            
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
            return jdbc.query(query, params, rs -> {
                if (rs.next())
                    return Optional.of(new AllCommunityInfoResponse(
                            rs.getLong("communityId"),
                            rs.getString("communityLocator"),
                            rs.getString("displayName"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("dateCreated")),
                            null,
                            rs.getInt("count")));

                return Optional.empty();
            });
        } catch (Exception e) {
            log.error("Exception occurred CommunityMemberDao.getAllCommunityInfoByCommunityLocator", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AllCommunityInfoResponse> getAllCommunityInfoByCommunityId(Long communityId) {
        final String query =
                """            
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
            return jdbc.query(query, params, rs -> {
                if (rs.next())
                    return Optional.of(new AllCommunityInfoResponse(
                            rs.getLong("communityId"),
                            rs.getString("communityLocator"),
                            rs.getString("displayName"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("dateCreated")),
                            null,
                            rs.getInt("count")));

                return Optional.empty();
            });
        } catch (Exception e) {
            log.error("Exception occurred CommunityMemberDao.getAllCommunityInfoByCommunityLocator", e);
            throw new RuntimeException(e);
        }
    }
}
