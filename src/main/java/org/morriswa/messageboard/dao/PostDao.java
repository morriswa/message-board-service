package org.morriswa.messageboard.dao;

import org.morriswa.messageboard.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostDao {

    Optional<Post> findPostByPostId(Long postId);

    List<Post> findAllPostsByCommunityId(Long communityId);

    void createNewPost(Post newPost);
}
