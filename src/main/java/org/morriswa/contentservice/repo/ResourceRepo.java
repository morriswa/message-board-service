package org.morriswa.contentservice.repo;

import org.morriswa.contentservice.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceRepo extends JpaRepository<Resource, UUID> {
}
