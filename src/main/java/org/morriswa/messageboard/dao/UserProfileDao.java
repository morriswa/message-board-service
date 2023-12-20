package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.CreateUserRequest;
import org.morriswa.messageboard.model.UpdateUIProfileRequest;
import org.morriswa.messageboard.model.User;
import org.morriswa.messageboard.model.UserUiProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileDao {

    Optional<User> getUser(String authZeroId);

    Optional<User> getUser(UUID userId);

    Optional<UUID> getUserId(String authZeroId);

    @Deprecated boolean existsByDisplayName(String displayName);

    void createNewUser(@Valid CreateUserRequest user) throws BadRequestException, ValidationException;

    void updateUserDisplayName(UUID userId, String displayName) throws ValidationException;

    UserUiProfile getUIProfile(UUID userId);

    void setUIProfile(UUID userId, UpdateUIProfileRequest uiProfile);
}
