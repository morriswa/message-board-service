package org.morriswa.messageboard.service;

import org.morriswa.messageboard.dao.CommunityDao;
import org.morriswa.messageboard.enumerated.CommunityResourceType;
import org.morriswa.messageboard.enumerated.CommunityStanding;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.exception.PermissionsException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.dao.CommunityMemberDao;
import org.morriswa.messageboard.store.CommunityResourceStore;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.blobTypeToImageFormat;

@Service
public class CommunityServiceImpl implements CommunityService {

    private final Environment e;
    private final CommunityDao communityDao;
    private final CommunityMemberDao communityMemberDao;
    private final UserProfileService userProfileService;
    private final CommunityServiceValidator validator;
    private final CommunityResourceStore resources;

    @Autowired
    public CommunityServiceImpl(Environment e,
                                CommunityDao communityDao,
                                CommunityMemberDao communityMemberDao, UserProfileService userProfileService,
                                CommunityServiceValidator validator,
                                CommunityResourceStore resources) {
        this.e = e;
        this.communityDao = communityDao;
        this.communityMemberDao = communityMemberDao;
        this.userProfileService = userProfileService;
        this.validator = validator;
        this.resources = resources;
    }

    private void userCanPromoteOrThrow(Community community, UUID requesterUserId, UUID requestedUserId) throws PermissionsException {
        if (community.ownerId().equals(requesterUserId))
            return;

        if (community.ownerId().equals(requestedUserId))
            throw new PermissionsException(e.getRequiredProperty("community.service.errors.edit-owner"));

        var requestedMembership =
                communityMemberDao.getWatcherStatus(requestedUserId, community.communityId());

        if (requestedMembership.moderationLevel().weight >= ModerationLevel.PROMOTE_MOD.weight)
            throw new PermissionsException(e.getRequiredProperty("community.service.errors.edit-promoter"));

        var requesterMembership =
                communityMemberDao.getWatcherStatus(requesterUserId, community.communityId());

        if (requesterMembership.moderationLevel().weight >= ModerationLevel.PROMOTE_MOD.weight)
            return;

        throw new PermissionsException();
    }

    private boolean userIsCommunityOwner(Community community, UUID requesterUserId) {
        return community.ownerId().equals(requesterUserId);
    }


    @Override
    public void assertUserHasPrivilegeInCommunity(UUID userId, ModerationLevel privilege, Long communityId) throws PermissionsException, BadRequestException {

        var community = communityDao.findCommunity(communityId)
                .orElseThrow(()-> new BadRequestException(
                        String.format(e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId)
                ));

        assertUserHasPrivilegeInCommunity(userId, privilege, community);

    }

    @Override
    public void assertUserHasPrivilegeInCommunity(UUID userId, ModerationLevel privilege, Community community) throws PermissionsException, BadRequestException {
        if (community.ownerId().equals(userId)) return;

        var requesterMembership =
                communityMemberDao.getWatcherStatus(userId, community.communityId());

        if (requesterMembership.moderationLevel().weight >= privilege.weight)
            return;

        throw new PermissionsException(
                String.format(
                        e.getRequiredProperty("community.service.errors.user-cannot-moderate"),
                        userId,
                        privilege,
                        community.communityId()
                ));
    }

    @Override
    public void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception {

        var community = communityDao.findCommunity(communityId)
                .orElseThrow(()-> new BadRequestException(
                    String.format(e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                    communityId)
                ));

        if (community.ownerId().equals(userId)) return;

        var requesterMembership =
                communityMemberDao.getWatcherStatus(userId, community.communityId());

        if (!requesterMembership.exists())
            throw new PermissionsException(
                    e.getRequiredProperty("community.service.errors.no-relation-found"));

        if (requesterMembership.standing().equals(CommunityStanding.HEALTHY))
            return;

        throw new PermissionsException(
                e.getRequiredProperty("community.service.errors.user-cannot-post"));
    }

    @Override
    public List<CommunityMember.Response> getCommunityModerators(JwtAuthenticationToken jwt, Long communityId) throws Exception {

        var userId = userProfileService.authenticate(jwt);

        var community = communityDao.findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityId)));

        assertUserHasPrivilegeInCommunity(userId, ModerationLevel.COMMENT_MOD, community);

        var members = communityMemberDao.getCommunityModerators(communityId);

        var membersResponse = new ArrayList<CommunityMember.Response>(members.size());
        members.forEach(member->{
            var profileImage = userProfileService.getProfileImage(member.userId());
            membersResponse.add(CommunityMember.buildMemberResponse(member, profileImage));
        });

        return membersResponse;
    }

    @Override
    public CommunityMember.Response getCommunityMemberInfo(JwtAuthenticationToken jwt, Long communityId, UUID userId) throws Exception {
        var requester = userProfileService.authenticate(jwt);

        var community = communityDao.findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityId)));

        assertUserHasPrivilegeInCommunity(requester, ModerationLevel.COMMENT_MOD, community);

        CommunityMember.Response member;
        if (userId.equals(community.ownerId()))
        {
            var profile = userProfileService.getUserProfile(userId);
            member = CommunityMember.buildOwnerResponse(profile);
        }
        else
        {
            var profile = communityMemberDao.findCommunityMemberByUserIdAndCommunityId(userId, communityId)
                    .orElseThrow(() -> new BadRequestException(
                            e.getRequiredProperty("community.service.errors.user-not-in-community")
                    ));

            var profileImage = userProfileService.getProfileImage(profile.userId());

            member = CommunityMember.buildMemberResponse(profile, profileImage);
        }
        return member;
    }

    private void verifyUserIsOwnerOrThrow(UUID requesterId, Community community) throws Exception {
        if (!userIsCommunityOwner(community, requesterId))
            throw new PermissionsException(String.format(
                    e.getRequiredProperty("community.service.errors.user-cannot-edit"),
                    requesterId,
                    community.communityId()
            ));
    }

    @Override
    public void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequest request) throws Exception {
        var userId = userProfileService.authenticate(token);

        validator.validate(request);

        communityDao.createNewCommunity(userId, request);
    }

    @Override
    public void updateCommunityResource(JwtAuthenticationToken token,
                                        MultipartFile image,
                                        Long communityId,
                                        CommunityResourceType resourceType) throws Exception {
        var userId = userProfileService.authenticate(token);

        var community = communityDao.findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityId)));

        assertUserHasPrivilegeInCommunity(userId, ModerationLevel.EDIT_MOD, community);
        var uploadImageRequest = new UploadImageRequest(image.getBytes(), blobTypeToImageFormat(Objects.requireNonNull(image.getContentType())));

        validator.validate(uploadImageRequest);

        if (resourceType.equals(CommunityResourceType.ICON)) resources.setCommunityIcon(uploadImageRequest, communityId);
        else if (resourceType.equals(CommunityResourceType.BANNER)) resources.setCommunityBanner(uploadImageRequest, communityId);
    }

    @Override
    public Community.Response getAllCommunityInfo(String communityLocator) throws Exception {

        Community community = communityDao
                .findCommunity(communityLocator)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityLocator)));

        var communityResources =
                resources.getAllCommunityResources(community.communityId());

        return new Community.Response(community, communityResources);
    }

    @Override
    public Community.Response getAllCommunityInfo(Long communityId) throws Exception {

        var community = communityDao
                .findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId.toString())));

        var communityResources =
                resources.getAllCommunityResources(community.communityId());

        return new Community.Response(community, communityResources);
    }

    @Override
    public void joinCommunity(JwtAuthenticationToken token, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(token);

        if (communityMemberDao.relationshipExists(userId,communityId))
            return;

        var newRelationship = new JoinCommunityRequest(userId, communityId,
                ModerationLevel.NONE, CommunityStanding.HEALTHY);

        validator.validateBeanOrThrow(newRelationship);

        communityMemberDao.createNewRelationship(newRelationship);
    }

    @Override
    public void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(token);

        communityMemberDao.deleteRelationship(userId, communityId);
    }

    @Override
    public List<Community.Response> getAllUsersCommunities(JwtAuthenticationToken token) throws Exception {

        var userId = userProfileService.authenticate(token);

        var communities = communityDao.findAllCommunities(userId);

        return new ArrayList<>() {{
            for (var community : communities)
                add(new Community.Response(community, resources.getAllCommunityResources(community.communityId())));
        }};
    }

    @Override
    public List<Community.Response> searchForCommunities(String searchText) {

        var communities = communityDao.searchForCommunities(searchText);

        return new ArrayList<>() {{
            for (var community : communities)
                add(new Community.Response(community, resources.getAllCommunityResources(community.communityId())));
        }};
    }



    @Override
    public void updateCommunityAttributes(JwtAuthenticationToken token,
                                          UpdateCommunityRequest request) throws Exception {

        validator.validate(request);

        var requesterId = userProfileService.authenticate(token);

        var community = communityDao.findCommunity(request.communityId())
                .orElseThrow(()->new BadRequestException(
                String.format(
                        e.getRequiredProperty("community.service.errors.missing-community"),
                        request.communityId())));

        if (request.communityOwnerUserId() != null)
            verifyUserIsOwnerOrThrow(requesterId, community);
        else assertUserHasPrivilegeInCommunity(requesterId, ModerationLevel.EDIT_MOD, community);

        communityDao.updateCommunityAttrs(community.communityId(), request);
    }


    @Override
    public CommunityWatcherStatus getWatcherStatus(JwtAuthenticationToken jwt, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(jwt);

        return communityMemberDao.getWatcherStatus(userId, communityId);
    }

    @Override
    public void updateCommunityMemberModerationLevel(JwtAuthenticationToken token, Long communityId, UUID userId, ModerationLevel level) throws Exception {
        var requesterUserId = userProfileService.authenticate(token);

        var communityInfo = communityDao
                .findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId.toString())));

        userCanPromoteOrThrow(communityInfo, requesterUserId, userId);

        if (!communityMemberDao.relationshipExists(userId, communityInfo.communityId()))
            throw new BadRequestException(e.getRequiredProperty("community.service.errors.user-not-in-community"));

        communityMemberDao.updateCommunityMemberModerationLevel(userId, communityInfo.communityId(), level);
    }

    @Override
    public URL getIcon(Long communityId) {
        return resources.getCommunityIcon(communityId);
    }

}
