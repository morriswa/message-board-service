package org.morriswa.messageboard.service;

import org.morriswa.messageboard.enumerated.CommunityResourceType;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.PermissionsException;
import org.morriswa.messageboard.model.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public interface CommunityService {

    void assertUserHasPrivilegeInCommunity(UUID userId, ModerationLevel privilege, Long communityId) throws PermissionsException, BadRequestException;

    void assertUserHasPrivilegeInCommunity(UUID userId, ModerationLevel privilege, Community community) throws PermissionsException, BadRequestException;

    void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception;

    void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequest request) throws Exception;

    void updateCommunityResource(JwtAuthenticationToken token, MultipartFile image, Long communityId, CommunityResourceType resourceType) throws Exception;

    void updateCommunityAttributes(JwtAuthenticationToken token, UpdateCommunityRequest request) throws Exception;

    Community.Response getAllCommunityInfo(String communityLocator) throws Exception;

    Community.Response getAllCommunityInfo(Long communityId) throws Exception;

    void joinCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws Exception;

    List<Community.Response> getAllUsersCommunities(JwtAuthenticationToken token) throws Exception;

    List<Community.Response> searchForCommunities(String searchText);

    CommunityWatcherStatus getWatcherStatus(JwtAuthenticationToken jwt, Long communityId) throws Exception;

    void updateCommunityMemberModerationLevel(JwtAuthenticationToken token, Long communityId, UUID userId, ModerationLevel level) throws Exception;

    URL getIcon(Long communityId);

    List<CommunityMember.Response> getCommunityModerators(JwtAuthenticationToken jwt, Long communityId) throws Exception;

    CommunityMember.Response getCommunityMemberInfo(JwtAuthenticationToken jwt, Long communityId, UUID userId) throws Exception;
}