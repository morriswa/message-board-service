package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepo extends JpaRepository<User, UUID> {

    Optional<User> findUserByAuthZeroId(String authZeroId);

    boolean existsByDisplayName(String displayName);

    Optional<User> findUserByUserId(UUID userId);
}
