package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.responsebody.PostCommunityResponse;
import org.morriswa.messageboard.model.responsebody.PostDetailsResponse;
import org.morriswa.messageboard.model.responsebody.PostDraftResponse;
import org.morriswa.messageboard.model.responsebody.PostUserResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentService {

    List<PostUserResponse> getFeedForCommunity(Long communityId) throws Exception;

    List<PostCommunityResponse> getRecentPosts() throws Exception;

    List<Comment> getComments(Long postId);

    List<Comment> getComments(Long postId, Long parentId);

    void leaveComment(JwtAuthenticationToken token, Long postId, String comment) throws Exception;

    void leaveComment(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws Exception;

    int voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws Exception;

    int voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws Exception;

    UUID createPostDraft(JwtAuthenticationToken token, Long communityId, Optional<String> caption, Optional<String> description) throws Exception;

    void editPostDraft(JwtAuthenticationToken token, UUID draftId, Optional<String> caption, Optional<String> description) throws Exception;

    void addContentToDraft(JwtAuthenticationToken token, UUID draftId, MultipartFile file) throws Exception;

    PostDraftResponse getPostDraft(JwtAuthenticationToken token, UUID draftId) throws Exception;

    void createPostFromDraft(JwtAuthenticationToken token, UUID draftId) throws Exception;

    PostDetailsResponse retrievePostDetails(JwtAuthenticationToken token, Long postId) throws Exception;
}
