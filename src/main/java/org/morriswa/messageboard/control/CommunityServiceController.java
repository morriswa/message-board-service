package org.morriswa.messageboard.control;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.NoRegisteredUserException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.requestbody.CreateCommunityRequestBody;
import org.morriswa.messageboard.service.CommunityService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

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

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community.messages.post"));
    }

    @GetMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> getAllCommunityInformation(@RequestParam String communityLocator) throws Exception {
        var response = this.community.getAllCommunityInfo(communityLocator);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.community.messages.get"),
                response);
    }

    @PatchMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> updateCommunityInformation(JwtAuthenticationToken token,
                                                        @RequestParam Long communityId,
                                                        @RequestParam Optional<String> communityRef,
                                                        @RequestParam Optional<String> communityDisplayName) throws Exception {
        community.updateCommunityAttributes(token, communityId, communityRef, communityDisplayName);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community.messages.patch"));
    }

    @PostMapping("${community.service.endpoints.update-community-banner.path}")
    public ResponseEntity<?> updateCommunityBanner(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId,
                                             @RequestPart MultipartFile image) throws Exception {
        this.community.updateCommunityBanner(jwt, image, communityId);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.update-community-banner.messages.post"));
    }

    @PostMapping("${community.service.endpoints.update-community-icon.path}")
    public ResponseEntity<?> updateCommunityIcon(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId,
                                                 @RequestPart("image") MultipartFile file) throws Exception {
        this.community.updateCommunityIcon(jwt, file, communityId);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.update-community-icon.messages.post"));
    }

    @GetMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> getCommunityMembershipInfo(JwtAuthenticationToken jwt,
                                           @PathVariable Long communityId) throws Exception {
        var membership = this.community.getCommunityMembershipInfo(jwt, communityId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("community.service.endpoints.community-membership.messages.get"),
                membership);
    }

    @PostMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> joinCommunity(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId) throws Exception {
        this.community.joinCommunity(jwt, communityId);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community-membership.messages.post"));
    }

    @DeleteMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> leaveCommunity(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId) throws Exception {
        this.community.leaveCommunity(jwt, communityId);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.community-membership.messages.delete"));
    }

    @GetMapping("${community.service.endpoints.get-users-communities.path}")
    public ResponseEntity<?> getUsersCommunities(JwtAuthenticationToken jwtAuthenticationToken) throws Exception {
        var communities = this.community.getAllUsersCommunities(jwtAuthenticationToken);

        return responseFactory.getResponse(
            HttpStatus.OK,
            e.getRequiredProperty("community.service.endpoints.get-users-communities.messages.get"),
            communities);
    }



}
