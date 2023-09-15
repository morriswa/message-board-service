package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.AllCommunityInfoResponse;
import org.morriswa.messageboard.model.CommunityStanding;
import org.morriswa.messageboard.model.CreateNewCommunityRequest;
import org.morriswa.messageboard.repo.CommunityMemberRepo;
import org.morriswa.messageboard.repo.CommunityRepo;
import org.morriswa.messageboard.service.util.CommunityResourceService;
import org.morriswa.messageboard.entity.Community;
import org.morriswa.messageboard.entity.CommunityMember;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service @Slf4j
public class CommunityServiceImpl implements CommunityService {

    private final Environment e;
    private final CommunityRepo communityRepo;
    private final CommunityMemberRepo communityMemberRepo;
    private final UserProfileService userProfileService;
    private final CommunityServiceValidator validator;
    private final CommunityResourceService resourceService;

    @Autowired
    public CommunityServiceImpl(Environment e,
                                CommunityRepo communityRepo,
                                CommunityMemberRepo communityMemberRepo, UserProfileService userProfileService,
                                CommunityServiceValidator validator,
                                CommunityResourceService resourceService) {
        this.e = e;
        this.communityRepo = communityRepo;
        this.communityMemberRepo = communityMemberRepo;
        this.userProfileService = userProfileService;
        this.validator = validator;
        this.resourceService = resourceService;
    }


    @Override
    public void createNewCommunity(JwtAuthenticationToken token, CreateNewCommunityRequest request) throws BadRequestException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        var newCommunity = new Community(request.getCommunityRef(), request.getCommunityName(), userId);

        this.validator.validateBeanOrThrow(newCommunity);

        communityRepo.save(newCommunity);
    }

    @Override
    public void updateCommunityIcon(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId)
                .orElseThrow(()->new BadRequestException(e.getRequiredProperty("community.service.errors.not-community-owner")));

        resourceService.setCommunityIcon(uploadImageRequest, communityId);
    }

    @Override
    public void updateCommunityBanner(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId)
                .orElseThrow(()->new BadRequestException(e.getRequiredProperty("community.service.errors.not-community-owner")));

        resourceService.setCommunityBanner(uploadImageRequest, communityId);
    }

    private AllCommunityInfoResponse buildAllCommunityInfoResponse(Community community) {
        int communityMembers = communityMemberRepo.countCommunityMembersByCommunityId(community.getCommunityId());

        var resources = resourceService.getAllCommunityResources(community.getCommunityId());

        return new AllCommunityInfoResponse(community, communityMembers, resources);
    }

    @Override
    public AllCommunityInfoResponse getAllCommunityInfo(String communityDisplayName) throws BadRequestException {

        var community = communityRepo
                .findCommunityByCommunityLocator(communityDisplayName)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityDisplayName)));

        return buildAllCommunityInfoResponse(community);
    }

    @Override
    public AllCommunityInfoResponse getAllCommunityInfo(Long communityId) throws BadRequestException {

        var community = communityRepo
                .findCommunityByCommunityId(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId.toString())));

        return buildAllCommunityInfoResponse(community);
    }

    @Override
    public void joinCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        var result = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId,communityId);

        if (result.isPresent())
            return;

        var newRelationship = new CommunityMember(userId, communityId);

        validator.validateBeanOrThrow(newRelationship);

        communityMemberRepo.save(newRelationship);
    }

    @Override
    public void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        var result = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId,communityId);

        if (result.isEmpty())
            return;

        communityMemberRepo.delete(result.get());
    }

    private boolean canUserPostInCommunity(UUID userId, Long communityId) throws BadRequestException {


        var checkCommunityOwner = communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId);

        if (checkCommunityOwner.isPresent()) return true;

        var communityMember = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("community.service.errors.no-relation-found")));

        if (communityMember.getCommunityStanding().equals(CommunityStanding.HEALTHY))
            return true;

        return false;
    }

    @Override
    public void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException {
        if (!this.canUserPostInCommunity(userId, communityId))
            throw new BadRequestException(
                    e.getRequiredProperty("community.service.errors.user-cannot-post")
            );
    }

    @Override
    public List<AllCommunityInfoResponse> getAllUsersCommunities(JwtAuthenticationToken token) throws BadRequestException {

        var user = userProfileService.authenticateAndGetUserEntity(token);

        var communities = communityMemberRepo.findAllByUserId(user.getUserId());

        var response = new ArrayList<AllCommunityInfoResponse>();

        for (CommunityMember community : communities) {
            response.add(getAllCommunityInfo(community.getCommunityId()));
        }

        return response;
    }
}
