package org.morriswa.contentservice.repo;

import org.morriswa.contentservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findAllCommentsByPostId(Long postId);
}
