package org.morriswa.communityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.common.model.BadRequestException;
import org.morriswa.common.model.UploadImageRequest;
import org.morriswa.communityservice.entity.Community;
import org.morriswa.communityservice.entity.CommunityMember;
import org.morriswa.communityservice.model.AllCommunityInfoResponse;
import org.morriswa.communityservice.model.CommunityStanding;
import org.morriswa.communityservice.model.CreateNewCommunityRequest;
import org.morriswa.communityservice.repo.CommunityMemberRepo;
import org.morriswa.communityservice.repo.CommunityRepo;
import org.morriswa.communityservice.service.util.ResourceService;
import org.morriswa.userprofileservice.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service @Slf4j
public class CommunityServiceImpl implements CommunityService {

    private final Environment e;
    private final CommunityRepo communityRepo;
    private final CommunityMemberRepo communityMemberRepo;
    private final UserProfileService userProfileService;
    private final CommunityServiceValidator validator;
    private final ResourceService resourceService;

    @Autowired
    public CommunityServiceImpl(Environment e,
                                CommunityRepo communityRepo,
                                CommunityMemberRepo communityMemberRepo, UserProfileService userProfileService,
                                CommunityServiceValidator validator,
                                ResourceService resourceService) {
        this.e = e;
        this.communityRepo = communityRepo;
        this.communityMemberRepo = communityMemberRepo;
        this.userProfileService = userProfileService;
        this.validator = validator;
        this.resourceService = resourceService;
    }


    @Override
    public void createNewCommunity(CreateNewCommunityRequest request) {
        var newCommunity = new Community(request.getCommunityName(), request.getUserId());

        this.validator.validateBeanOrThrow(newCommunity);

        communityRepo.save(newCommunity);
    }

    @Override
    public void updateCommunityIcon(UploadImageRequest uploadImageRequest, Long communityId, String jwt) throws BadRequestException, IOException {
        var userId = userProfileService.getUserId(jwt);

        communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId)
                .orElseThrow(()->new BadRequestException(e.getRequiredProperty("community.service.errors.not-community-owner")));

        resourceService.setCommunityIcon(uploadImageRequest, communityId);
    }

    @Override
    public void updateCommunityBanner(UploadImageRequest uploadImageRequest, Long communityId, String jwt) throws BadRequestException, IOException {
        var userId = userProfileService.getUserId(jwt);

        communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId)
                .orElseThrow(()->new BadRequestException(e.getRequiredProperty("community.service.errors.not-community-owner")));

        resourceService.setCommunityBanner(uploadImageRequest, communityId);
    }

    @Override
    public AllCommunityInfoResponse getAllCommunityInfo(String communityDisplayName) throws BadRequestException {
        var response = new AllCommunityInfoResponse();

        var community = communityRepo
                .findCommunityByCommunityDisplayName(communityDisplayName)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getProperty("community.service.errors.missing-community"),
                                communityDisplayName)));

        response.setCommunityId(community.getCommunityId());
        response.setDisplayName(community.getCommunityDisplayName());
        response.setOwnerId(community.getCommunityOwnerUserId());
        response.setDateCreated(community.getDateCreated());

        var resources = resourceService.getAllCommunityResources(community.getCommunityId());

        response.setResourceUrls(resources);

        return response;
    }

    @Override
    public void joinCommunity(String authzeroid, Long communityId) throws BadRequestException {
        var userId = userProfileService.getUserId(authzeroid);

        var result = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId,communityId);

        if (result.isPresent())
            return;

        var newRelationship = new CommunityMember(userId, communityId);

        validator.validateBeanOrThrow(newRelationship);

        communityMemberRepo.save(newRelationship);
    }

    @Override
    public void leaveCommunity(String authzeroid, Long communityId) throws BadRequestException {
        var userId = userProfileService.getUserId(authzeroid);

        var result = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId,communityId);

        if (result.isEmpty())
            return;

        communityMemberRepo.delete(result.get());
    }

    @Override
    public boolean canUserPostInCommunity(UUID userId, Long communityId) throws BadRequestException {
        var communityMember = communityMemberRepo.findCommunityMemberByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("community.service.errors.no-relation-found")));

        var checkCommunityOwner = communityRepo.findCommunityByCommunityIdAndCommunityOwnerUserId(communityId, userId);

        if (checkCommunityOwner.isPresent()) return true;

        if (communityMember.getCommunityStanding().equals(CommunityStanding.HEALTHY))
            return true;

        return false;
    }
}
