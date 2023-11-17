package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component @Slf4j
public class ResourceDaoImpl implements ResourceDao{
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper om;

    @Autowired
    public ResourceDaoImpl(NamedParameterJdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }


    @Override
    public Optional<Resource> findResourceByResourceId(UUID resourceId) {
        final String query = "select * from post_resource where id=:id";

        Map<String, Object> params = new HashMap<>(){{
           put("id", resourceId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                final String data = rs.getString("data");

                final List<String> format;
                try {
                    format = om.readValue(data, ArrayList.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                final List<UUID> resources = new ArrayList<>(10);
                format.forEach(rawString->{
                    resources.add(UUID.fromString(rawString));
                });

                final Resource resource = new Resource(rs.getObject("id", UUID.class), resources);
                return Optional.of(resource);
            }

            return Optional.empty();
        });
    }

    @Override
    public void createNewPostResource(Resource newResource) throws JsonProcessingException {
        final String query =
                """
                    insert into post_resource(id, data)
                    values(:id, :data)
                """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", newResource.getId());
            put("data", om.writeValueAsString(newResource.getResources()));
        }};

        jdbc.update(query, params);
    }

    @Override
    public void updateResource(Resource resource) throws JsonProcessingException {
        final String query = """
            update post_resource
                set data=:data
             where id=:id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", resource.getId());
            put("data", om.writeValueAsString(resource.getResources()));
        }};

        jdbc.update(query,params);
    }
}
