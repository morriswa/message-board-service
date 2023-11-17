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
import org.morriswa.messageboard.model.responsebody.PostResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    List<PostResponse> getFeedForCommunity(Long communityId) throws BadRequestException, ResourceException;

    List<Comment> getComments(Long postId);

    List<Comment> getComments(Long postId, Long parentId);

    void leaveComment(JwtAuthenticationToken token, Long postId, String comment) throws BadRequestException;

    void leaveComment(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws BadRequestException;

    int voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws BadRequestException;

    void voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws BadRequestException;

    UUID createPostDraft(JwtAuthenticationToken token, Long communityId, Optional<String> caption, Optional<String> description) throws BadRequestException, ResourceException;

    void editPostDraft(JwtAuthenticationToken token, UUID draftId, Optional<String> caption, Optional<String> description) throws BadRequestException;

    void addContentToDraft(JwtAuthenticationToken token, UUID draftId, MultipartFile file) throws BadRequestException, IOException, ValidationException, ResourceException;

    PostDraftResponse getPostDraft(JwtAuthenticationToken token, UUID draftId) throws BadRequestException, ResourceException;

    void createPostFromDraft(JwtAuthenticationToken token, UUID draftId) throws BadRequestException, ResourceException;

}
