package org.morriswa.messageboard.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.model.UserUiProfile;
import org.morriswa.messageboard.control.requestbody.UpdateUIProfileRequest;
import org.morriswa.messageboard.validation.request.CreateUserRequest;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileDao {

    Optional<User> getUser(String authZeroId);

    Optional<User> getUser(UUID userId);

    Optional<UUID> getUserId(String authZeroId);

    boolean existsByDisplayName(String displayName);

    void createNewUser(@Valid CreateUserRequest user) throws ValidationException, JsonProcessingException;

    void updateUserDisplayName(UUID userId, String displayName) throws ValidationException;

    UserUiProfile getUIProfile(UUID userId);

    void setUIProfile(UUID userId, UpdateUIProfileRequest uiProfile);
}
