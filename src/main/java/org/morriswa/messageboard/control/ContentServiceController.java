package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.PostContentType;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
import org.morriswa.messageboard.model.responsebody.CommentRequestBody;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController @CrossOrigin @Slf4j
@RequestMapping("${server.path}")
public class ContentServiceController {
    private final Environment e;
    private final ContentService contentService;
    private final HttpResponseFactoryImpl responseFactory;

    @Autowired
    public ContentServiceController(Environment e, ContentService contentService, HttpResponseFactoryImpl responseFactory) {
        this.e = e;
        this.contentService = contentService;
        this.responseFactory = responseFactory;
    }

    @PostMapping(value = "${content.service.endpoints.create-post.path}")
    public ResponseEntity<?> createPost(JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestPart("image") MultipartFile file,
                                        @RequestParam("caption") String caption,
                                        @RequestParam("description") String description,
                                        @RequestParam("contentType") PostContentType type)
            throws BadRequestException, ValidationException, IOException {

        log.info("Attempting to upload type: {}", file.getContentType());

        contentService.createPost(token, communityId, new CreatePostRequestBody(
                caption,
                description,
                type
        ), file);
        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.create-post.messages.post"));
    }

    @GetMapping("${content.service.endpoints.community-feed.path}")
    public ResponseEntity<?> getCommunityFeed(@PathVariable Long communityId) throws BadRequestException {

        var feed = contentService.getFeedForCommunity(communityId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.community-feed.messages.get"),
                feed);
    }

    @PostMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> leaveCommentOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @RequestParam Optional<Long> parentId,
                                                @RequestBody String comment) throws BadRequestException {
        if (parentId.isPresent()) {
            contentService.addCommentToPost(token, postId, parentId.get(), comment);
        }

        contentService.addCommentToPost(token, postId, comment);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.post"));
    }

    @GetMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) throws BadRequestException {
        var comments = contentService.getComments(postId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.get"),
                comments);
    }

}
