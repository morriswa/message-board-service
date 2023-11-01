package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component @Slf4j
public class ResourceDaoImpl implements ResourceDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public ResourceDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Optional<Resource> findResourceByResourceId(UUID resourceId) {
        final String query = "select * from post_resource where id=:id";

        Map<String, Object> params = new HashMap<>(){{
           put("id", resourceId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {

                List<UUID> uuids = new ArrayList<>();

                UUID found = rs.getObject("id", UUID.class);

//                log
                for (int i = 1; found != null && i <= 9; i++) {
                    uuids.add(found);
                    found = rs.getObject("id"+i, UUID.class);
                }

                Resource resource = new Resource();
                try {
                    resource.setList(uuids);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return Optional.of(resource);
            }

            return Optional.empty();
        });
    }

    @Override
    public void createNewPostResource(Resource newResource) {
        final String query =
                """
                    insert into post_resource(id, id1, id2, id3, id4, id5, id6, id7, id8, id9)
                    values(:id, :id1, :id2, :id3, :id4, :id5, :id6, :id7, :id8, :id9)
                """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", newResource.getResourceId());
            put("id1", newResource.getResourceId1());
            put("id2", newResource.getResourceId2());
            put("id3", newResource.getResourceId3());
            put("id4", newResource.getResourceId4());
            put("id5", newResource.getResourceId5());
            put("id6", newResource.getResourceId6());
            put("id7", newResource.getResourceId7());
            put("id8", newResource.getResourceId8());
            put("id9", newResource.getResourceId9());
        }};

        jdbc.update(query, params);
    }
}
