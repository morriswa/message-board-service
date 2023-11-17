package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.validatedrequest.CreatePostRequest;
import org.morriswa.messageboard.model.entity.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostDao {

    Optional<Post> findPostByPostId(Long postId);

    List<Post> findAllPostsByCommunityId(Long communityId);

    void createNewPost(@Valid CreatePostRequest newCreatePostRequest);

    int vote(UUID userId, Long postId, Vote vote);
}
