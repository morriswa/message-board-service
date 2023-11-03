package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.CreateUserRequest;
import org.morriswa.messageboard.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileDao {

    Optional<UserProfile> getUserProfile(String authZeroId);

    boolean existsByDisplayName(String displayName);

    Optional<UserProfile> getUserProfile(UUID userId);

    void createNewUser(@Valid CreateUserRequest user);

    void updateUserDisplayName(UUID userId, String displayName);

    Optional<UUID> getUserId(String authZeroId);

}
