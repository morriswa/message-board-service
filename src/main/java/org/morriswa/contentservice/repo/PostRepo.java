package org.morriswa.contentservice.repo;

import org.morriswa.contentservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {
    Optional<Post> findPostByPostId(Long postId);
}
