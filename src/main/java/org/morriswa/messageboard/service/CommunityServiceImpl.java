package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.dao.CommunityDao;
import org.morriswa.messageboard.enumerated.CommunityResourceType;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.model.CommunityMembership;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.dao.CommunityMemberDao;
import org.morriswa.messageboard.control.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.morriswa.messageboard.validation.request.UploadImageRequest;
import org.morriswa.messageboard.model.CommunityResponse;
import org.morriswa.messageboard.validation.request.CreateCommunityRequest;
import org.morriswa.messageboard.validation.request.JoinCommunityRequest;
import org.morriswa.messageboard.store.CommunityResourceStore;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.morriswa.messageboard.util.Functions.blobTypeToImageFormat;

@Service @Slf4j
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

    private boolean userHasPromoteAccessInCommunity(Community community, UUID requesterUserId) {
        if (community.getOwnerId().equals(requesterUserId)) return true;

        var requesterMembership =
                communityMemberDao.retrieveRelationship(requesterUserId, community.getCommunityId());

        if (requesterMembership.getModerationLevel().weight > ModerationLevel.PROMOTE_MOD.weight) return true;

        return false;
    }

    private boolean userHasEditAccessInCommunity(Community community, UUID requesterUserId) {
        if (community.getOwnerId().equals(requesterUserId)) return true;

        var requesterMembership =
                communityMemberDao.retrieveRelationship(requesterUserId, community.getCommunityId());

        if (requesterMembership.getModerationLevel().weight > ModerationLevel.PROMOTE_MOD.weight) return true;

        return false;
    }

    private boolean userIsCommunityOwner(Community community, UUID requesterUserId) {
        return community.getOwnerId().equals(requesterUserId);
    }


    @Override
    public void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception {
        if (!communityDao.verifyUserCanPostInCommunity(userId, communityId))
            throw new BadRequestException(
                    e.getRequiredProperty("community.service.errors.user-cannot-post")
            );
    }

    @Override
    public void verifyUserCanEditCommunityOrThrow(UUID userId, Community community) throws Exception {
        if (!userHasEditAccessInCommunity(community, userId))
            throw new BadRequestException(String.format(
                    e.getRequiredProperty("community.service.errors.user-cannot-edit"),
                    userId,
                    community.getCommunityId()
            ));
    }

    private void verifyUserIsOwnerOrThrow(UUID requesterId, Community community) throws BadRequestException {
        if (!userIsCommunityOwner(community, requesterId))
            throw new BadRequestException(String.format(
                    e.getRequiredProperty("community.service.errors.user-cannot-edit"),
                    requesterId,
                    community.getCommunityId()
            ));
    }

    @Override
    public void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws Exception {
        var userId = userProfileService.authenticate(token);

        var newCommunity = new CreateCommunityRequest(request.communityRef(), request.communityName(), userId);

        this.validator.validateBeanOrThrow(newCommunity);

        communityDao.createNewCommunity(newCommunity);
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

        verifyUserCanEditCommunityOrThrow(userId, community);
        var uploadImageRequest = new UploadImageRequest(image.getBytes(), blobTypeToImageFormat(Objects.requireNonNull(image.getContentType())));

        validator.validate(uploadImageRequest);

        if (resourceType.equals(CommunityResourceType.ICON)) resources.setCommunityIcon(uploadImageRequest, communityId);
        else if (resourceType.equals(CommunityResourceType.BANNER)) resources.setCommunityBanner(uploadImageRequest, communityId);
    }

    @Override
    public CommunityResponse getAllCommunityInfo(String communityLocator) throws Exception {

        Community community = communityDao
                .findCommunity(communityLocator)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityLocator)));

        var communityResources =
                resources.getAllCommunityResources(community.getCommunityId());

        return new CommunityResponse(community, communityResources);
    }

    @Override
    public CommunityResponse getAllCommunityInfo(Long communityId) throws Exception {

        var community = communityDao
                .findCommunity(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId.toString())));

        var communityResources =
                resources.getAllCommunityResources(community.getCommunityId());

        return new CommunityResponse(community, communityResources);
    }

    @Override
    public void joinCommunity(JwtAuthenticationToken token, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(token);

        if (communityMemberDao.relationshipExists(userId,communityId))
            return;

        var newRelationship = new JoinCommunityRequest(userId, communityId);

        validator.validateBeanOrThrow(newRelationship);

        communityMemberDao.createNewRelationship(newRelationship);
    }

    @Override
    public void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(token);

        communityMemberDao.deleteRelationship(userId, communityId);
    }

    @Override
    public List<CommunityResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws Exception {

        var userId = userProfileService.authenticate(token);

        var communities = communityDao.findAllCommunities(userId);

        return new ArrayList<>() {{
            for (var community : communities)
                add(new CommunityResponse(community, resources.getAllCommunityResources(community.getCommunityId())));
        }};
    }

    @Override
    public List<CommunityResponse> searchForCommunities(String searchText) {

        var communities = communityDao.searchForCommunities(searchText);

        return new ArrayList<>() {{
            for (var community : communities)
                add(new CommunityResponse(community, resources.getAllCommunityResources(community.getCommunityId())));
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
        else verifyUserCanEditCommunityOrThrow(requesterId, community);

        communityDao.updateCommunityAttrs(community.getCommunityId(), request);
    }


    @Override
    public CommunityMembership getCommunityMembershipInfo(JwtAuthenticationToken jwt, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(jwt);

        return communityMemberDao.retrieveRelationship(userId, communityId);
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

        if (!userHasPromoteAccessInCommunity(communityInfo, requesterUserId))
            throw new BadRequestException("NO");

        communityMemberDao.updateCommunityMemberModerationLevel(userId, communityId, level);
    }

}
