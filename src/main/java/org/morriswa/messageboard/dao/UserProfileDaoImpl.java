package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.UserUiProfile;
import org.morriswa.messageboard.control.requestbody.UpdateUIProfileRequest;
import org.morriswa.messageboard.validation.request.CreateUserRequest;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.enumerated.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
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

    private final Environment environment;
    private final NamedParameterJdbcTemplate jdbc;

    private static final String AUTH0_UNIQUE_CONSTRAINT_VIOLATION = "duplicate key value violates unique constraint \"user_profile_auth_zero_id_key\"";
    private static final String DISPLAY_NAME_UNIQUE_CONSTRAINT_VIOLATION = "duplicate key value violates unique constraint \"user_profile_display_name_key\"";

    @Autowired
    public UserProfileDaoImpl(Environment environment, NamedParameterJdbcTemplate jdbc) {
        this.environment = environment;
        this.jdbc = jdbc;
    }

    private Optional<User> unwrapUserResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) return Optional.of(new User(
            rs.getObject("id", UUID.class),
            rs.getString("auth_zero_id"),
            rs.getString("email"),
            rs.getString("display_name"),
            UserRole.valueOf(rs.getString("role"))));

        return Optional.empty();
    }
    @Override
    public Optional<User> getUser(UUID userId) {

        final String query = "select id, auth_zero_id, display_name, email, role from user_profile where id=:userId";

        Map<String, Object> params = new HashMap<>(){{
           put("userId", userId);
        }};

        return jdbc.query(query, params, this::unwrapUserResultSet);
    }

    @Override
    public Optional<User> getUser(String authZeroId) {

        final String query = "select id, auth_zero_id, display_name, email, role from user_profile where auth_zero_id=:authZeroId";

        Map<String, Object> params = new HashMap<>(){{
            put("authZeroId", authZeroId);
        }};

        return jdbc.query(query, params, this::unwrapUserResultSet);
    }

    @Override
    public void createNewUser(@Valid CreateUserRequest user) throws ValidationException, JsonProcessingException {
        final String query = """
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
        } catch (DuplicateKeyException e) {
            if (e.getMostSpecificCause().getMessage().contains(AUTH0_UNIQUE_CONSTRAINT_VIOLATION)) {
                throw new ValidationException("user",user.getClass().getSimpleName(), "User is already registered!");
            }

            if (e.getMostSpecificCause().getMessage().contains(DISPLAY_NAME_UNIQUE_CONSTRAINT_VIOLATION)) {
                throw new ValidationException("displayName",user.getDisplayName(),
                        environment.getRequiredProperty("user-profile.service.errors.display-name-already-exists"));
            }

            log.error("encountered unexpected error in data layer ", e);
            throw e;
        }
    }

    @Override
    public void updateUserDisplayName(UUID userId, String requestedDisplayName) throws ValidationException {
        final String query = """
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
        } catch (DuplicateKeyException e) {
            if (e.getMostSpecificCause().getMessage().contains(DISPLAY_NAME_UNIQUE_CONSTRAINT_VIOLATION)) {
                throw new ValidationException("displayName",requestedDisplayName,
                        environment.getRequiredProperty("user-profile.service.errors.display-name-already-exists"));
            }

            log.error("encountered unexpected error in data layer ", e);
            throw e;
        }
    }

    @Override
    public UserUiProfile getUIProfile(UUID userId) {
        final String query = "select * from user_ui_profile where user_id=:userId";

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) {
                return new UserUiProfile(rs.getObject("user_id", UUID.class), rs.getString("theme"));
            }

            return new UserUiProfile(userId, environment.getRequiredProperty("user-profile.service.rules.default-theme"));
        });
    }

    private void createNewUIProfile(UUID userId, UpdateUIProfileRequest uiProfile) {
        final String queryUpdateUiProfile = "insert into user_ui_profile (user_id, theme) values (:userId, :theme)";

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            if (!uiProfile.theme().isBlank()) put("theme", uiProfile.theme());
        }};

        jdbc.update(queryUpdateUiProfile, params);
    }

    private void updateExistingUIProfile(UUID userId, UpdateUIProfileRequest uiProfile) {
        final String queryUpdateUiProfile = "update user_ui_profile set theme=:theme where user_id=:userId";

        Map<String, Object> params = new HashMap<>(){{
            put("userId", userId);
            if (!uiProfile.theme().isBlank()) put("theme", uiProfile.theme());
        }};

        jdbc.update(queryUpdateUiProfile, params);
    }

    @Override
    public void setUIProfile(UUID userId, UpdateUIProfileRequest uiProfile) {
        final String queryUiProfileExists = "select 1 from user_ui_profile where user_id=:userId";

        Map<String, Object> paramsUiProfileExists = new HashMap<>(){{
            put("userId", userId);
        }};

        final boolean profileExists = Boolean.TRUE.equals(
                jdbc.query(queryUiProfileExists, paramsUiProfileExists, ResultSet::next));

        if (profileExists) updateExistingUIProfile(userId, uiProfile);
        else createNewUIProfile(userId, uiProfile);
    }

    @Override
    public Optional<UUID> getUserId(String authZeroId) {

        final String query = "select id from user_profile where auth_zero_id=:authZeroId";

        Map<String, Object> params = new HashMap<>(){{
            put("authZeroId", authZeroId);
        }};

        return jdbc.query(query, params, rs -> {
            if (rs.next()) return Optional.of(
                rs.getObject("id", UUID.class));
            return Optional.empty();
        });
    }

    @Override
    public boolean existsByDisplayName(String displayName) {

        final String query = "select 1 from user_profile where display_name=:displayName";

        Map<String, Object> params = new HashMap<>(){{
            put("displayName", displayName);
        }};

        return Boolean.TRUE.equals(jdbc.query(query, params, ResultSet::next));
    }
}
