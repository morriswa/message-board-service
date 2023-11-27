package org.morriswa.messageboard.control;

import org.morriswa.messageboard.control.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.enumerated.CommunityResourceType;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.model.CommunityResponse;
import org.morriswa.messageboard.service.CommunityService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.morriswa.messageboard.control.requestbody.UpdateCommunityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class CommunityServiceController {
    private final Environment e;
    private final CommunityService community;
    private final HttpResponseFactoryImpl responseFactory;

    @Autowired
    public CommunityServiceController(Environment e, CommunityService community, HttpResponseFactoryImpl responseFactory) {
        this.e = e;
        this.community = community;
        this.responseFactory = responseFactory;
    }

    @PostMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> createCommunity(JwtAuthenticationToken jwt,
                                             @RequestBody CreateCommunityRequestBody request) throws Exception {
        this.community.createNewCommunity(jwt, request);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community.messages.post"));
    }

    @GetMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> getAllCommunityInformation(@RequestParam String communityLocator) throws Exception {
        CommunityResponse response = this.community.getAllCommunityInfo(communityLocator);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.community.messages.get"),
                response);
    }

    @PatchMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> updateCommunityInformation(JwtAuthenticationToken token,
                                                        @RequestBody UpdateCommunityRequest request) throws Exception {
        community.updateCommunityAttributes(token, request);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community.messages.patch"));
    }

    @PostMapping("${community.service.endpoints.update-community-banner.path}")
    public ResponseEntity<?> updateCommunityBanner(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId,
                                             @RequestPart MultipartFile image) throws Exception {
        this.community.updateCommunityResource(jwt, image, communityId, CommunityResourceType.BANNER);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.update-community-banner.messages.post"));
    }

    @PostMapping("${community.service.endpoints.update-community-icon.path}")
    public ResponseEntity<?> updateCommunityIcon(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId,
                                                 @RequestPart("image") MultipartFile file) throws Exception {
        this.community.updateCommunityResource(jwt, file, communityId, CommunityResourceType.ICON);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.update-community-icon.messages.post"));
    }

    @GetMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> getCommunityMembershipInfo(JwtAuthenticationToken jwt,
                                           @PathVariable Long communityId) throws Exception {
        var membership = this.community.getCommunityMembershipInfo(jwt, communityId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.community-membership.messages.get"),
                membership);
    }

    @PostMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> joinCommunity(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId) throws Exception {
        this.community.joinCommunity(jwt, communityId);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community-membership.messages.post"));
    }

    @DeleteMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> leaveCommunity(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId) throws Exception {
        this.community.leaveCommunity(jwt, communityId);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community-membership.messages.delete"));
    }

    @GetMapping("${community.service.endpoints.get-users-communities.path}")
    public ResponseEntity<?> getUsersCommunities(JwtAuthenticationToken jwtAuthenticationToken) throws Exception {
        List<CommunityResponse> communities = this.community.getAllUsersCommunities(jwtAuthenticationToken);

        return responseFactory.build(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.get-users-communities.messages.get"),
            communities);
    }

    @GetMapping("${community.service.endpoints.search-communities.path}")
    public ResponseEntity<?> findCommunities(@RequestParam String searchText) {
        List<CommunityResponse> communities = this.community.searchForCommunities(searchText);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.search-communities.messages.get"),
                communities);
    }

    @PatchMapping("${community.service.endpoints.community-moderation.path}")
    public ResponseEntity<?> updateCommunityMemberModerationLevel(JwtAuthenticationToken token,
                                                                  @PathVariable Long communityId,
                                                                  @RequestParam UUID userId,
                                                                  @RequestParam ModerationLevel promote) throws Exception {
        this.community.updateCommunityMemberModerationLevel(token, communityId, userId, promote);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.community-moderation.messages.patch"));
    }

}
