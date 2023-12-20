package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.model.PostWithCommunityInfo;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.model.CreatePostRequest;
import org.morriswa.messageboard.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostDao {

    Optional<Post> findPostByPostId(Long postId);

    List<Post> findAllPostsByCommunityId(Long communityId);

    List<PostWithCommunityInfo> getMostRecent();

    List<PostWithCommunityInfo> getMostRecent(int endSlice);

    List<PostWithCommunityInfo> getMostRecent(int startSlice, int endSlice);

    void createNewPost(@Valid CreatePostRequest newCreatePostRequest);

    int vote(UUID userId, Long postId, Vote vote);

    void deletePost(Long postId);
}
