package org.morriswa.messageboard.service;

import org.morriswa.messageboard.control.requestbody.DraftBody;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.model.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ContentService {

    List<PostResponse> getFeedForCommunity(Long communityId) throws Exception;

    List<PostCommunityResponse> getRecentPosts() throws Exception;

    List<Comment> getComments(Long postId);

    List<Comment> getComments(Long postId, Long parentId);

    void leaveComment(JwtAuthenticationToken token, Long postId, String comment) throws Exception;

    void leaveComment(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws Exception;

    int voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws Exception;

    int voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws Exception;

    UUID createPostDraft(JwtAuthenticationToken token, Long communityId, DraftBody draft) throws Exception;

    void editPostDraft(JwtAuthenticationToken token, UUID draftId, DraftBody draft) throws Exception;

    void addContentToDraft(JwtAuthenticationToken token, UUID draftId, MultipartFile file) throws Exception;

    PostDraftResponse getPostDraft(JwtAuthenticationToken token, UUID draftId) throws Exception;

    void createPostFromDraft(JwtAuthenticationToken token, UUID draftId) throws Exception;

    PostCommentResponse retrievePostDetails(JwtAuthenticationToken token, Long postId) throws Exception;

    void deletePost(JwtAuthenticationToken token, Long postId) throws Exception;

    void deleteComment(JwtAuthenticationToken token, Long postId, Long commentId) throws Exception;
}
