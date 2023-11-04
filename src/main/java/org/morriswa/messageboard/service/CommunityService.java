package org.morriswa.messageboard.service;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.model.requestbody.CreateCommunityRequestBody;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityService {

    void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws BadRequestException;

    void updateCommunityIcon(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException, ValidationException;

    void updateCommunityBanner(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException, ValidationException;

    CommunityResponse getAllCommunityInfo(String communityLocator) throws BadRequestException;

    CommunityResponse getAllCommunityInfo(Long communityId) throws BadRequestException;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    void verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    List<CommunityResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws BadRequestException;

    void updateCommunityAttributes(JwtAuthenticationToken token, Long communityId, Optional<String> communityRef, Optional<String> communityDisplayName) throws BadRequestException, ValidationException;
}