package org.morriswa.messageboard.control;

import org.morriswa.messageboard.model.BadRequestException;
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
        request.setAuthZeroId(jwt.getName());
        this.community.createNewCommunity(request);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.community.messages.post")
        ));
    }

    @GetMapping("${community.service.endpoints.community.path}")
    public ResponseEntity<?> getAllCommunityInformation(@RequestParam String displayName) throws BadRequestException {
        var response = this.community.getAllCommunityInfo(displayName);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.community.messages.get"),
                response));
    }

    @PostMapping("${community.service.endpoints.update-community-banner.path}")
    public ResponseEntity<?> updateCommunityBanner(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId,
                                             @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
        this.community.updateCommunityBanner(request,communityId,jwt.getName());

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.update-community-banner.messages.post")
        ));
    }

    @PostMapping("${community.service.endpoints.update-community-icon.path}")
    public ResponseEntity<?> updateCommunityIcon(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId,
                                                 @RequestBody UploadImageRequest request) throws BadRequestException, IOException {
        this.community.updateCommunityIcon(request,communityId, jwt.getName());

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.update-community-icon.messages.post")
        ));
    }

    @PostMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> joinCommunity(JwtAuthenticationToken jwt,
                                             @PathVariable Long communityId) throws BadRequestException {
        this.community.joinCommunity(jwt.getName(),communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.community-membership.messages.post")
        ));
    }

    @DeleteMapping("${community.service.endpoints.community-membership.path}")
    public ResponseEntity<?> leaveCommunity(JwtAuthenticationToken jwt,
                                                 @PathVariable Long communityId) throws BadRequestException {
        this.community.leaveCommunity(jwt.getName(),communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("community.service.endpoints.community-membership.messages.delete")
        ));
    }





}
