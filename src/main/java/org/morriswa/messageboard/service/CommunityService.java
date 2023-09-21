package org.morriswa.messageboard.service;

import org.morriswa.messageboard.entity.Community;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.AllCommunityInfoResponse;
import org.morriswa.messageboard.model.CreateNewCommunityRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityService {

    void createNewCommunity(JwtAuthenticationToken token, CreateNewCommunityRequest request) throws BadRequestException;

    void updateCommunityIcon(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException;

    void updateCommunityBanner(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException;

    AllCommunityInfoResponse getAllCommunityInfo(String communityDisplayName) throws BadRequestException;

    AllCommunityInfoResponse getAllCommunityInfo(Long communityId) throws BadRequestException;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    Community verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    List<AllCommunityInfoResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws BadRequestException;

    void updateCommunityAttributes(JwtAuthenticationToken token, Long communityId, Optional<String> communityRef, Optional<String> communityDisplayName) throws BadRequestException, ValidationException;
}