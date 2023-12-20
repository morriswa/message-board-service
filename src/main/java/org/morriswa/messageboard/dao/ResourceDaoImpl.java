package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ResourceDaoImpl implements ResourceDao{
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper om;

    @Autowired
    public ResourceDaoImpl(NamedParameterJdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }


    @Override
    public Optional<Resource> findResourceByResourceId(UUID resourceId) throws ResourceException {
        final String query = "select * from post_resource where id=:id";

        Map<String, Object> params = new HashMap<>(){{
           put("id", resourceId);
        }};

        Map<String, Object> rawResult = jdbc.query(query, params, rs -> {
            Map<String, Object> result = new HashMap<>();

            if (rs.next()) {
                result.put("id",rs.getObject("id", UUID.class));
                result.put("data",rs.getString("data"));
            }

            return result;
        });

        if (rawResult == null || !rawResult.containsKey("id"))
            return Optional.empty();

        final String data = (String) rawResult.getOrDefault("data", "[]");

        final List<?> format;
        try {
            format = om.readValue(data, ArrayList.class);
        } catch (JsonProcessingException e) {
            throw new ResourceException(e.getMessage());
        }

        final List<UUID> resources = new ArrayList<>(10);
        format.forEach(rawString->{
            resources.add(UUID.fromString((String) rawString));
        });

        return Optional.of(new Resource((UUID) rawResult.get("id"), resources));
    }

    @Override
    public void createNewPostResource(Resource resource) throws ResourceException {
        final String query =
                """
                    insert into post_resource(id, data)
                    values(:id, :data)
                """;

        final String data;
        try {
            data =  om.writeValueAsString(resource.getResources());
        } catch (JsonProcessingException jpe) {
            throw new ResourceException(jpe.getMessage());
        }

        Map<String, Object> params = new HashMap<>(){{
            put("id", resource.getId());
            put("data", data);
        }};

        jdbc.update(query, params);
    }

    @Override
    public void updateResource(Resource resource) throws ResourceException {
        final String query = """
            update post_resource
                set data=:data
             where id=:id
        """;

        final String data;
        try {
            data =  om.writeValueAsString(resource.getResources());
        } catch (JsonProcessingException jpe) {
            throw new ResourceException(jpe.getMessage());
        }

        Map<String, Object> params = new HashMap<>(){{
            put("id", resource.getId());
            put("data",data);
        }};

        jdbc.update(query,params);
    }

    @Override
    public void deleteResource(UUID resourceId) {
        final String query = """
            delete from post_resource
            where id=:id
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("id", resourceId);
        }};

        jdbc.update(query, params);
    }
}
