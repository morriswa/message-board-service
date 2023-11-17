package org.morriswa.messageboard.service;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.entity.CommunityMembership;
import org.morriswa.messageboard.model.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityService {

    void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws BadRequestException;

    void updateCommunityIcon(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws BadRequestException, ValidationException, IOException;

    void updateCommunityBanner(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws BadRequestException, ValidationException, IOException;

    CommunityResponse getAllCommunityInfo(String communityLocator) throws BadRequestException;

    CommunityResponse getAllCommunityInfo(Long communityId) throws BadRequestException;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException;

    void verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException;

    List<CommunityResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws BadRequestException;

    void updateCommunityAttributes(JwtAuthenticationToken token, Long communityId, Optional<String> communityRef, Optional<String> communityDisplayName) throws BadRequestException, ValidationException;

    CommunityMembership getCommunityMembershipInfo(JwtAuthenticationToken jwt, Long communityId) throws BadRequestException;
}