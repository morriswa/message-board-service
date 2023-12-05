package org.morriswa.messageboard.service;

import org.morriswa.messageboard.enumerated.CommunityResourceType;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.CommunityMembership;
import org.morriswa.messageboard.control.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.model.CommunityModeratorResponse;
import org.morriswa.messageboard.model.CommunityResponse;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface CommunityService {

//    void verifyUserCanEditCommunityOrThrow(UUID userId, Community community) throws Exception;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception;

    void verifyUserCanModerateContentOrThrow(UUID userId, Long communityId) throws Exception;

    void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws Exception;

    void updateCommunityResource(JwtAuthenticationToken token, MultipartFile image, Long communityId, CommunityResourceType resourceType) throws Exception;

    void updateCommunityAttributes(JwtAuthenticationToken token, UpdateCommunityRequest request) throws Exception;

    CommunityResponse getAllCommunityInfo(String communityLocator) throws Exception;

    CommunityResponse getAllCommunityInfo(Long communityId) throws Exception;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    List<CommunityResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws Exception;

    List<CommunityResponse> searchForCommunities(String searchText);

    CommunityMembership getCommunityMembershipInfo(JwtAuthenticationToken jwt, Long communityId) throws Exception;

    void updateCommunityMemberModerationLevel(JwtAuthenticationToken token, Long communityId, UUID userId, ModerationLevel level) throws Exception;

    URL getIcon(Long communityId);

    void verifyUserCanModerateCommentsOrThrow(UUID userId, Long communityId) throws BadRequestException, Exception;

    List<CommunityModeratorResponse> getCommunityModerators(JwtAuthenticationToken jwt, Long communityId) throws Exception;
}