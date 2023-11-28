package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.validation.request.CreateCommunityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.timestampToGregorian;

@Component @Slf4j
public class CommunityDaoImpl implements CommunityDao {

    private final Environment environment;
    private final NamedParameterJdbcTemplate jdbc;
    private static final String COMMUNITY_REF_UNIQUE_CONSTRAINT_VIOLATION = "duplicate key value violates unique constraint \"community_community_ref_key\"";

    @Autowired
    CommunityDaoImpl(Environment environment, NamedParameterJdbcTemplate jdbc) {
        this.environment = environment;
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
    }

    @Override
    public void createNewCommunity(CreateCommunityRequest newCreateCommunityRequest) throws ValidationException {
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
        } catch (DuplicateKeyException dke) {
            if (dke.getMostSpecificCause().getMessage()
                    .contains(COMMUNITY_REF_UNIQUE_CONSTRAINT_VIOLATION)) {

                throw new ValidationException("communityLocator",
                        newCreateCommunityRequest.getCommunityLocator(),
                        environment.getRequiredProperty("community.service.errors.ref-already-taken")
                );
            }

            log.error("encountered unexpected error ", dke);
            throw dke;
        }
    }

    @Override
    public void updateCommunityAttrs(Long communityId, UpdateCommunityRequest attributesToUpdate) throws ValidationException {
        StringBuilder queryBuilder = new StringBuilder("update community set ");

        var params = new HashMap<String,Object>(){{
            put("communityId", communityId);
        }};

        if (attributesToUpdate.communityLocator() != null) {
            queryBuilder.append("community_ref=:locator,");
            params.put("locator", attributesToUpdate.communityLocator());
        }

        if (attributesToUpdate.communityDisplayName() != null) {
            queryBuilder.append("display_name=:displayName,");
            params.put("displayName", attributesToUpdate.communityDisplayName());
        }

        if (attributesToUpdate.communityOwnerUserId() != null) {
            queryBuilder.append("owner=:ownerId,");
            params.put("ownerId", attributesToUpdate.communityOwnerUserId());
        }

        queryBuilder.deleteCharAt(queryBuilder.length() - 1);

        queryBuilder.append(" where id=:communityId");

        final String query = queryBuilder.toString();

        try {
            jdbc.update(query, params);
        } catch (DuplicateKeyException dke) {
            if (dke.getMostSpecificCause().getMessage()
                    .contains(COMMUNITY_REF_UNIQUE_CONSTRAINT_VIOLATION)) {

                throw new ValidationException("communityLocator",
                        attributesToUpdate.communityLocator(),
                        environment.getRequiredProperty("community.service.errors.ref-already-taken")
                );
            }

            log.error("encountered unexpected error!!!", dke);
            throw dke;
        }
    }

    @Override
    public List<Community> searchForCommunities(String searchText) {
        final String query = """            
            select
                *,
                (select count(cme.id) from community_member cme where community.id=cme.community_id) AS count

            from community
            where to_tsvector(display_name) @@ to_tsquery(:search)
            limit 5
        """;

        StringBuilder fullSearchQuery = new StringBuilder();
        for (String word : searchText.split("\\s"))
            fullSearchQuery.append(String.format(" %s &", word));

        fullSearchQuery.delete(fullSearchQuery.length() - 1, fullSearchQuery.length());

        Map<String, Object> params = new HashMap<>(){{
            put("search", fullSearchQuery.toString());
        }};

        try {
            return jdbc.query(query, params, rs -> {
                List<Community> communities = new ArrayList<>();

                while (rs.next()) {
                    communities.add(new Community(
                            rs.getLong("id"),
                            rs.getString("community_ref"),
                            rs.getString("display_name"),
                            rs.getObject("owner", UUID.class),
                            timestampToGregorian(rs.getTimestamp("date_created")),
                            rs.getInt("count")));
                }

                return communities;
            });
        } catch (Exception e) {
            return new ArrayList<>();
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

        return jdbc.query(query, params, this::unwrapCommunityResultSet);
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

        return jdbc.query(query, params, this::unwrapCommunityResultSet);
    }
}
