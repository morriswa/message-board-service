package org.morriswa.messageboard.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.responsebody.PostDraftResponse;
import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
import org.morriswa.messageboard.model.responsebody.PostResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    @Deprecated void createPost(JwtAuthenticationToken token, Long communityId, CreatePostRequestBody request, MultipartFile... file) throws BadRequestException, ValidationException, IOException, ResourceException;

//    @Deprecated List<Comment> getFullCommentMapForPost(Long postId);

    List<PostResponse> getFeedForCommunity(Long communityId) throws BadRequestException, ResourceException;

    List<Comment> getPostComments(Long postId);

    List<Comment> getPostComments(Long postId, Long parentId);

    void addCommentToPost(JwtAuthenticationToken token, Long postId, String comment) throws BadRequestException;

    void addCommentToPost(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws BadRequestException;

    void voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws BadRequestException;

    void voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws BadRequestException;

    UUID startPostCreateSession(JwtAuthenticationToken token, Long communityId, Optional<String> caption, Optional<String> description) throws BadRequestException, ResourceException;

    void editPostDraft(JwtAuthenticationToken token, UUID sessionToken, Optional<String> caption, Optional<String> description) throws BadRequestException;

    void addContentToDraft(JwtAuthenticationToken token, UUID sessionToken, MultipartFile file) throws BadRequestException, IOException, ValidationException, ResourceException;

    PostDraftResponse getPostDraft(JwtAuthenticationToken token, UUID sessionToken) throws BadRequestException, ResourceException;

    void createPostFromDraft(JwtAuthenticationToken token, UUID sessionToken) throws BadRequestException, ResourceException;

}
