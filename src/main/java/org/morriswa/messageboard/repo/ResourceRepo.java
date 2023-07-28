package org.morriswa.messageboard.repo;

import org.morriswa.messageboard.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceRepo extends JpaRepository<Resource, UUID> {
}
