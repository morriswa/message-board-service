package org.morriswa.messageboard.dao;

import jakarta.validation.Valid;
import org.morriswa.messageboard.dao.model.PostWithCommunityInfoRow;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.validation.request.CreatePostRequest;
import org.morriswa.messageboard.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostDao {

    Optional<Post> findPostByPostId(Long postId);

    List<Post> findAllPostsByCommunityId(Long communityId);

    List<PostWithCommunityInfoRow> getMostRecent();

    List<PostWithCommunityInfoRow> getMostRecent(int endSlice);

    List<PostWithCommunityInfoRow> getMostRecent(int startSlice, int endSlice);

    void createNewPost(@Valid CreatePostRequest newCreatePostRequest);

    int vote(UUID userId, Long postId, Vote vote);

    void deletePost(Long postId);
}
