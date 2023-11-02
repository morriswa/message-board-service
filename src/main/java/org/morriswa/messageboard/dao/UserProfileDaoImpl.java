package org.morriswa.messageboard.dao;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component @Slf4j
public class UserProfileDaoImpl implements UserProfileDao {

    private final NamedParameterJdbcTemplate jdbc;

    @Autowired
    public UserProfileDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Optional<User> unwrapUserResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            User user = new User(
                    rs.getObject("id", UUID.class),
                    rs.getString("auth_zero_id"),
                    UserRole.valueOf(rs.getString("role")),
                    rs.getString("display_name"),
                    rs.getString("email")
            );

            return Optional.of(user);
        }

        return Optional.empty();
    }
    @Override
    public Optional<User> findUserByUserId(UUID userId) {

        final String query = "select id, auth_zero_id, display_name, email, role from user_profile where id=:userId";

        Map<String, Object> params = new HashMap<>(){{
           put("userId", userId);
        }};

        return jdbc.query(query, params, this::unwrapUserResultSet);
    }

    @Override
    public Optional<User> findUserByAuthZeroId(String authZeroId) {

        final String query = "select id, auth_zero_id, display_name, email, role from user_profile where auth_zero_id=:authZeroId";

        Map<String, Object> params = new HashMap<>(){{
            put("authZeroId", authZeroId);
        }};

        return jdbc.query(query, params, this::unwrapUserResultSet);
    }

    @Override
    public void createNewUser(User user) {
        final String query =
        """
            insert into user_profile(id, auth_zero_id, display_name, email, role)
            values (gen_random_uuid(), :authZeroId, :displayName, :email, :role)
        """;

        Map<String, Object> params = new HashMap<>(){{
            put("authZeroId", user.getAuthZeroId());
            put("displayName", user.getDisplayName());
            put("email", user.getEmail());
            put("role", user.getRole().toString());
        }};


        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }

    }

    @Override
    public void updateUserDisplayName(UUID userId, String requestedDisplayName) {
        final String query =
                """
                    update user_profile
                        set display_name = :displayName
                    where id = :userId
                """;

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            put("displayName",requestedDisplayName);
        }};

        try {
            jdbc.update(query, params);
        } catch (Exception e) {
            log.error("encountered error ", e);
        }
    }

    @Override
    public boolean existsByDisplayName(String displayName) {
        final String query = "select 1 from user_profile where display_name=:displayName";

        Map<String, Object> params = new HashMap<>(){{
            put("displayName", displayName);
        }};

        return jdbc.query(query, params, ResultSet::next);
    }
}
