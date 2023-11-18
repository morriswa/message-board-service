package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.entity.CommunityMembership;
import org.morriswa.messageboard.model.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.model.responsebody.CommunityResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityService {

    void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws Exception;

    void updateCommunityIcon(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws Exception;

    void updateCommunityBanner(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws Exception;

    CommunityResponse getAllCommunityInfo(String communityLocator) throws Exception;

    CommunityResponse getAllCommunityInfo(Long communityId) throws Exception;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    void verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws Exception;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception;

    List<CommunityResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws Exception;

    void updateCommunityAttributes(JwtAuthenticationToken token, Long communityId, Optional<String> communityRef, Optional<String> communityDisplayName) throws Exception;

    CommunityMembership getCommunityMembershipInfo(JwtAuthenticationToken jwt, Long communityId) throws Exception;
}