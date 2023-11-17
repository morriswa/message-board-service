package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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

    @GetMapping(value = "${content.service.endpoints.post-session.path}")
    public ResponseEntity<?> getSession(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId) throws BadRequestException, ResourceException {

        var draft = contentService.getPostDraft(token, sessionId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-session.messages.get"),
                draft);
    }

    @PostMapping(value = "${content.service.endpoints.post-session.path}")
    public ResponseEntity<?> createPost(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId)
            throws BadRequestException, ResourceException {

        contentService.createPostFromDraft(token, sessionId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-session.messages.post")
        );
    }

    @PatchMapping(value = "${content.service.endpoints.post-session.path}")
    public ResponseEntity<?> editPostDraft(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId,
            @RequestParam Optional<String> caption,
            @RequestParam Optional<String> description)
            throws BadRequestException {

        contentService.editPostDraft(token, sessionId, caption, description);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-session.messages.patch"));
    }

    @PostMapping(value = "${content.service.endpoints.create-post-session.path}")
    public ResponseEntity<?> startPostSession(
                                        JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestParam Optional<String> caption,
                                        @RequestParam Optional<String> description)
            throws BadRequestException, ResourceException {

        var id = contentService.createPostDraft(token, communityId, caption, description);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.create-post-session.messages.post"),
                id);
    }

    @PostMapping(value = "${content.service.endpoints.add-content.path}")
    public ResponseEntity<?> addContent(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId,
            @RequestPart MultipartFile content)
            throws BadRequestException, ValidationException, IOException, ResourceException {

        contentService.addContentToDraft(token, sessionId, content);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.add-content.messages.post"));
    }

    @GetMapping("${content.service.endpoints.community-feed.path}")
    public ResponseEntity<?> getCommunityFeed(@PathVariable Long communityId) throws BadRequestException, ResourceException {

        var feed = contentService.getFeedForCommunity(communityId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.community-feed.messages.get"),
                feed);
    }

    @PostMapping("${content.service.endpoints.post-voting.path}")
    public ResponseEntity<?> voteOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @RequestParam Vote vote) throws BadRequestException {
        contentService.voteOnPost(token, postId, vote);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-voting.messages.post"));
    }

    @PostMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> leaveCommentOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @RequestBody String comment) throws BadRequestException {

        contentService.leaveComment(token, postId, comment);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.post"));
    }

    @PostMapping("${content.service.endpoints.sub-comment.path}")
    public ResponseEntity<?> leaveCommentOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @PathVariable Long parentId,
                                                @RequestBody String comment) throws BadRequestException {

        contentService.leaveComment(token, postId, parentId, comment);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.sub-comment.messages.post"));
    }

    @GetMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) throws BadRequestException {
        var comments = contentService.getComments(postId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.get"),
                comments);
    }

    @GetMapping("${content.service.endpoints.sub-comment.path}")
    public ResponseEntity<?> getSubComments(@PathVariable Long postId,
                                            @PathVariable Long parentId) throws BadRequestException {
        var comments = contentService.getComments(postId, parentId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.sub-comment.messages.get"),
                comments);
    }

    @PostMapping("${content.service.endpoints.comment-voting.path}")
    public ResponseEntity<?> voteOnComment(JwtAuthenticationToken token,
                                        @PathVariable Long postId,
                                        @PathVariable Long commentId,
                                        @RequestParam Vote vote) throws BadRequestException {
        contentService.voteOnComment(token, postId, commentId, vote);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment-voting.messages.post"));
    }
}
