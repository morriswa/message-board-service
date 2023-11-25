package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.dao.CommunityDao;
import org.morriswa.messageboard.model.CommunityMembership;
import org.morriswa.messageboard.model.Community;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.dao.CommunityMemberDao;
import org.morriswa.messageboard.control.requestbody.CreateCommunityRequestBody;
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

    @Override
    public void createNewCommunity(JwtAuthenticationToken token, CreateCommunityRequestBody request) throws Exception {
        var userId = userProfileService.authenticate(token);

        var newCommunity = new CreateCommunityRequest(request.communityRef(), request.communityName(), userId);

        this.validator.validateBeanOrThrow(newCommunity);

        communityDao.createNewCommunity(newCommunity);
    }

    @Override
    public void updateCommunityIcon(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws Exception{
        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        var uploadImageRequest = new UploadImageRequest(image.getBytes(), blobTypeToImageFormat(Objects.requireNonNull(image.getContentType())));

        validator.validate(uploadImageRequest);

        resources.setCommunityIcon(uploadImageRequest, communityId);
    }

    @Override
    public void updateCommunityBanner(JwtAuthenticationToken token, MultipartFile image, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        var uploadImageRequest = new UploadImageRequest(image.getBytes(), blobTypeToImageFormat(Objects.requireNonNull(image.getContentType())));

        validator.validate(uploadImageRequest);

        resources.setCommunityBanner(uploadImageRequest, communityId);
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
    public void verifyUserCanPostInCommunityOrThrow(UUID userId, Long communityId) throws Exception {
        if (!communityDao.verifyUserCanPostInCommunity(userId, communityId))
            throw new BadRequestException(
                    e.getRequiredProperty("community.service.errors.user-cannot-post")
            );
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
    public void verifyUserCanEditCommunityOrThrow(UUID userId, Long communityId) throws Exception {
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
                                          Optional<String> communityDisplayName) throws Exception{

        var userId = userProfileService.authenticate(token);

        verifyUserCanEditCommunityOrThrow(userId, communityId);

        var community = communityDao.findCommunity(communityId)
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

    @Override
    public CommunityMembership getCommunityMembershipInfo(JwtAuthenticationToken jwt, Long communityId) throws Exception {
        var userId = userProfileService.authenticate(jwt);

        return communityMemberDao.retrieveRelationship(userId, communityId);
    }
}
