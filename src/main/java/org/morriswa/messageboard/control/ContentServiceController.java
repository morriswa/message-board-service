package org.morriswa.messageboard.control;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.model.DefaultResponse;
import org.morriswa.messageboard.model.NewPostRequest;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class ContentServiceController {
    private final Environment e;
    private final UserProfileService userProfileService;
    private final ContentService contentService;

    @Autowired
    public ContentServiceController(Environment e, UserProfileService userProfileService, ContentService contentService) {
        this.e = e;
        this.userProfileService = userProfileService;
        this.contentService = contentService;
    }

    @GetMapping("${content.service.endpoints.user-id.path}")
    public ResponseEntity<?> sayHiToUserProfile(JwtAuthenticationToken token) throws BadRequestException, UnsupportedEncodingException {
        var userId = userProfileService.getUserId(token.getName());
        return ResponseEntity.ok(new DefaultResponse<>(
            e.getProperty("content.service.endpoints.user-id.messages.get"),
            userId));
    }

    @PostMapping("${content.service.endpoints.create-post.path}")
    public ResponseEntity<?> createPost(JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestBody NewPostRequest request) throws BadRequestException, UnsupportedEncodingException, IOException {
        request.setAuthZeroId(token.getName());
        request.setCommunityId(communityId);
        contentService.createPost(request);
        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("content.service.endpoints.create-post.messages.post")));
    }

    @GetMapping("${content.service.endpoints.community-feed.path}")
    public ResponseEntity<?> createPost(JwtAuthenticationToken token,
                                        @PathVariable Long communityId) throws BadRequestException {

//        contentService.createPost(request);
        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("content.service.endpoints.community-feed.messages.get")));
    }

}
