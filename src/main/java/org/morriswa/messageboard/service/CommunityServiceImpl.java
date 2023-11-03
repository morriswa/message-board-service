package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.dao.CommunityDao;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.model.CreateNewCommunityRequest;
import org.morriswa.messageboard.dao.CommunityMemberDao;
import org.morriswa.messageboard.stores.CommunityResourceStore;
import org.morriswa.messageboard.model.NewCommunityRequest;
import org.morriswa.messageboard.model.CommunityMember;
import org.morriswa.messageboard.validation.CommunityServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public void createNewCommunity(JwtAuthenticationToken token, CreateNewCommunityRequest request) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        var newCommunity = new NewCommunityRequest(request.getCommunityRef(), request.getCommunityName(), userId);

        this.validator.validateBeanOrThrow(newCommunity);

        communityDao.createNewCommunity(newCommunity);
    }

    @Override
    public void updateCommunityIcon(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException {
        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        resources.setCommunityIcon(uploadImageRequest, communityId);
    }

    @Override
    public void updateCommunityBanner(JwtAuthenticationToken token, UploadImageRequest uploadImageRequest, Long communityId) throws BadRequestException, IOException {
        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        resources.setCommunityBanner(uploadImageRequest, communityId);
    }

    @Override
    public Community getAllCommunityInfo(String communityLocator) throws BadRequestException {

        Community community = communityDao
                .getAllCommunityInfo(communityLocator)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community"),
                                communityLocator)));

        community.setResourceUrls(
                resources.getAllCommunityResources(community.getCommunityId())
        );

        return community;
    }

    @Override
    public Community getAllCommunityInfo(Long communityId) throws BadRequestException {

        var community = communityDao
                .getAllCommunityInfo(communityId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("community.service.errors.missing-community-by-id"),
                                communityId.toString())));

        community.setResourceUrls(
                resources.getAllCommunityResources(community.getCommunityId())
        );

        return community;
    }

    @Override
    public void joinCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        if (communityMemberDao.relationshipExists(userId,communityId))
            return;

        var newRelationship = new CommunityMember(userId, communityId);

        validator.validateBeanOrThrow(newRelationship);

        communityMemberDao.createNewRelationship(newRelationship);
    }

    @Override
    public void leaveCommunity(JwtAuthenticationToken token, Long communityId) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        communityMemberDao.deleteRelationship(userId, communityId);
    }

    @Override
    public void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException {
        if (!communityDao.verifyUserCanPostInCommunity(userId, communityId))
            throw new BadRequestException(
                    e.getRequiredProperty("community.service.errors.user-cannot-post")
            );
    }

    @Override
    public List<Community> getAllUsersCommunities(JwtAuthenticationToken token) throws BadRequestException {

        var userId = userProfileService.authenticate(token);

        var communities = communityDao.findAllCommunitiesByUserId(userId);

        for (var community : communities)
            community.setResourceUrls(resources.getAllCommunityResources(community.getCommunityId()));

        return communities;
    }

    @Override
    public void verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws BadRequestException {
        if (!communityDao.verifyUserCanEditCommunity(userId, communityId))
            throw new BadRequestException(String.format(
                    e.getRequiredProperty("community.service.errors.user-cannot-edit"),
                    userId,
                    communityId
            ));
    }

    @Override
    public void updateCommunityAttributes(JwtAuthenticationToken token,
                                          Long communityId,
                                          Optional<String> communityRef,
                                          Optional<String> communityDisplayName) throws BadRequestException, ValidationException {

        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        var community = communityDao.getAllCommunityInfo(communityId)
                .orElseThrow(()->new BadRequestException(
                String.format(
                        e.getRequiredProperty("community.service.errors.missing-community"),
                        communityId)));

        if (communityRef.isPresent()) {
            var requestedRef = communityRef.get();
            // if the user requested the same name they already had, ignore
            if (!requestedRef.equals(community.getCommunityLocator())) {
                if (communityDao.existsByCommunityLocator(requestedRef))
                    throw new BadRequestException(
                            e.getRequiredProperty("community.service.errors.ref-already-taken")
                    );
                validator.validateCommunityRefOrThrow(requestedRef);
                communityDao.setCommunityLocator(communityId, requestedRef);
            }
        }

        if (communityDisplayName.isPresent()) {
            var displayName = communityDisplayName.get();
            validator.validateCommunityDisplayNameOrThrow(displayName);
            communityDao.setCommunityDisplayName(communityId, displayName);
        }

    }
}
