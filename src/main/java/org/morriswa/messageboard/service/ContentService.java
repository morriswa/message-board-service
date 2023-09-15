package org.morriswa.messageboard.service;

import java.io.IOException;
import java.util.List;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface ContentService {

    void createPost(JwtAuthenticationToken token, Long communityId, NewPostRequest request) throws BadRequestException, IOException;

    void addCommentToPost(JwtAuthenticationToken token, NewCommentRequest request) throws BadRequestException;

    List<CommentResponse> getFullCommentMapForPost(Long postId);

    List<PhotosPostResponse> getFeedForCommunity(Long communityId) throws BadRequestException;
}
