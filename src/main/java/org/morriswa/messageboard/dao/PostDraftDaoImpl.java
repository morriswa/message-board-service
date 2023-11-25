package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.PostDraft;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component @Slf4j
public class PostDraftDaoImpl implements PostDraftDao {
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public PostDraftDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }



    @Override
    public void create(UUID id, UUID userId, Long communityId, UUID resourceId, Optional<String> caption, Optional<String> description) {
        final String query = """
            insert into post_session (id, user_id, community_id, resource_id, caption, description)
            values(:id, :userId, :communityId, :resourceId, :caption, :description)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", id);
            put("userId", userId);
            put("communityId", communityId);
            put("resourceId", resourceId);
            put("caption", caption.orElse(null));
            put("description", description.orElse(null));
        }};

        jdbc.update(query, params);
    }

    @Override
    public Optional<PostDraft> getDraft(UUID draftId) {
        final String query = """
            select * from post_session where id=:id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", draftId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                return Optional.of(new PostDraft(
                        rs.getObject("id", UUID.class),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getObject("resource_id", UUID.class),
                        rs.getString("caption"),
                        rs.getString("description")));
            }

            return Optional.empty();
        });
    }

    @Override
    public void edit(UUID userId, UUID draftId, Optional<String> caption, Optional<String> description) {
        StringBuilder queryBuilder = new StringBuilder();

        Map<String, Object> params = new HashMap<>(){{
            put("id", draftId);
            put("userId", userId);
        }};

        queryBuilder.append("update post_session set ");
        if (caption.isPresent()) {
            queryBuilder.append("caption=:caption ");
            params.put("caption", caption.get());
        }

        if (description.isPresent()) {
            if (caption.isPresent()) queryBuilder.append(", ");
            queryBuilder.append("description=:description ");
            params.put("description", description.get());
        }

        queryBuilder.append("where user_id=:userId and id=:id");
        final String query = queryBuilder.toString();

        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearUsersDrafts(UUID userId) {
        final String query = """
            delete from post_session where user_id=:userId
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
        }};

        jdbc.update(query, params);
    }
}
