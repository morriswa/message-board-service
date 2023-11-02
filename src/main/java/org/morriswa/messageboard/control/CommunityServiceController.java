package org.morriswa.messageboard.control;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.DefaultResponse;
import org.morriswa.messageboard.model.UploadImageRequest;
import org.morriswa.messageboard.model.CreateNewCommunityRequest;
import org.morriswa.messageboard.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class CommunityServiceController {
    private final Environment e;
    private final CommunityService community;

    @Autowired
    public CommunityServiceController(Environment e, CommunityService community) {
        this.e = e;
        this.community = community;
    }

    @PostMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> createCommunity(JwtAuthenticationToken jwt,
                                             @RequestBody CreateNewCommunityRequest request) throws BadRequestException {
        this.community.createNewCommunity(jwt, request);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.community.messages.post")
        ));
    }

    @GetMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> getAllCommunityInformation(@RequestParam String communityLocator) throws BadRequestException {
        var response = this.community.getAllCommunityInfo(communityLocator);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.community.messages.get"),
                response));
    }

    @PatchMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> updateCommunityInformation(JwtAuthenticationToken token,
                                                        @RequestParam Long communityId,
                                                        @RequestParam Optional<String> communityRef,
                                                        @RequestParam Optional<String> communityDisplayName) throws BadRequestException, ValidationException {
        community.updateCommunityAttributes(token, communityId, communityRef, communityDisplayName);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.community.messages.patch")
                ));
    }

    @PostMapping("${community.service.endpoints.update-community-banner.path}")
    public ResponseEntity<?> updateCommunityBanner(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId,
                                             @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
        this.community.updateCommunityBanner(jwt, request, communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.update-community-banner.messages.post")
        ));
    }

    @PostMapping("${community.service.endpoints.update-community-icon.path}")
    public ResponseEntity<?> updateCommunityIcon(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId,
                                                 @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
        this.community.updateCommunityIcon(jwt, request, communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.update-community-icon.messages.post")
        ));
    }

    @PostMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> joinCommunity(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId) throws BadRequestException {
        this.community.joinCommunity(jwt, communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.community-membership.messages.post")
        ));
    }

    @DeleteMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> leaveCommunity(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId) throws BadRequestException {
        this.community.leaveCommunity(jwt, communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.community-membership.messages.delete")
        ));
    }

    @GetMapping("${community.service.endpoints.get-users-communities.path}")
    public ResponseEntity<?> getUsersCommunities(JwtAuthenticationToken jwtAuthenticationToken) throws BadRequestException {
        var communities = this.community.getAllUsersCommunities(jwtAuthenticationToken);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getRequiredProperty("community.service.endpoints.get-users-communities.messages.get"),
                communities
        ));
    }



}
