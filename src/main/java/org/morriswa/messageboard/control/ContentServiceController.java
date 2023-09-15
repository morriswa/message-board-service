package org.morriswa.messageboard.control;

import java.io.IOException;

import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.DefaultResponse;
import org.morriswa.messageboard.model.NewPostRequest;
import org.morriswa.messageboard.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController @CrossOrigin
@RequestMapping("${server.path}")
public class ContentServiceController {
    private final Environment e;
    private final ContentService contentService;

    @Autowired
    public ContentServiceController(Environment e, ContentService contentService) {
        this.e = e;
        this.contentService = contentService;
    }

    @PostMapping("${content.service.endpoints.create-post.path}")
    public ResponseEntity<?> createPost(JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestBody NewPostRequest request) throws BadRequestException, IOException {
        contentService.createPost(token, communityId, request);
        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("content.service.endpoints.create-post.messages.post")));
    }

    @GetMapping("${content.service.endpoints.community-feed.path}")
    public ResponseEntity<?> getCommunityFeed(@PathVariable Long communityId) throws BadRequestException {

        var feed = contentService.getFeedForCommunity(communityId);

        return ResponseEntity.ok(new DefaultResponse<>(
                e.getProperty("content.service.endpoints.community-feed.messages.get"),
                feed));
    }

}
