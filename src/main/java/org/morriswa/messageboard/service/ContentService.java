package org.morriswa.messageboard.service;

import java.io.IOException;
import java.util.List;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
import org.morriswa.messageboard.model.responsebody.PostResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    void createPost(JwtAuthenticationToken token, Long communityId, CreatePostRequestBody request, MultipartFile file) throws BadRequestException, ValidationException, IOException;

    List<Comment> getFullCommentMapForPost(Long postId);

    List<PostResponse> getFeedForCommunity(Long communityId) throws BadRequestException;

    List<Comment> getComments(Long postId) throws BadRequestException;

    void addCommentToPost(JwtAuthenticationToken token, Long postId, String comment) throws BadRequestException;

    void addCommentToPost(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws BadRequestException;
}
