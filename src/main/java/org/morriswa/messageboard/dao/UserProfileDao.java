package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileDao {

    Optional<User> findUserByAuthZeroId(String authZeroId);

    boolean existsByDisplayName(String displayName);

    Optional<User> findUserByUserId(UUID userId);

    void createNewUser(User user);

    void updateUserDisplayName(UUID userId, String displayName);
}
