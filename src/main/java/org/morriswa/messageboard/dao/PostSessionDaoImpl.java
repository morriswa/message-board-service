package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.PostSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component @Slf4j
public class PostSessionDaoImpl implements PostSessionDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public PostSessionDaoImpl(NamedParameterJdbcTemplate jdbc) {
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
    public PostSession getSession(UUID sessionToken) {
        final String query = """
            select * from post_session where id=:id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", sessionToken);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                return new PostSession(
                        rs.getObject("id", UUID.class),
                        rs.getObject("user_id", UUID.class),
                        rs.getLong("community_id"),
                        rs.getObject("resource_id", UUID.class),
                        rs.getString("caption"),
                        rs.getString("description")
                );
            }

            return new PostSession();
        });
    }
}
