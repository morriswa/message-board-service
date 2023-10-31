package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ResourceDaoImpl implements ResourceDao{
    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public ResourceDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public Optional<Resource> findResourceByResourceId(UUID resourceId) {
        return Optional.empty();
    }

    @Override
    public void createNewPostResource(Resource newResource) {

    }
}
