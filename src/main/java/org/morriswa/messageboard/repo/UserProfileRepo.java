package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepo extends JpaRepository<User, UUID> {
    Optional<User> findUserByAuthZeroId(String authZeroId);
}
