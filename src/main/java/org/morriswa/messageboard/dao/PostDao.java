package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostDao {

    Optional<Post> findPostByPostId(Long postId);

    List<Post> findAllPostsByCommunityId(Long communityId);

    void createNewPost(@Valid Post newPost);
}
