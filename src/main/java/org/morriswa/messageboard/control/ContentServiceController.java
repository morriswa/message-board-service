package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.PostContentType;
import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

    private static List<Parameter> getParameterNames(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.getName().startsWith("file")).toList();
    }

    @GetMapping(value = "${content.service.endpoints.post-session.path}")
    public ResponseEntity<?> getSession(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId) throws BadRequestException, ResourceException {

        var sessionz = contentService.getSession(token, sessionId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-session.messages.get"),
                sessionz);
    }

    @PostMapping(value = "${content.service.endpoints.post-session.path}")
    public ResponseEntity<?> postDraft(
            JwtAuthenticationToken token,
            @PathVariable UUID sessionId)
            throws BadRequestException, ResourceException {

        contentService.postDraft(token, sessionId);

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
            throws BadRequestException, ResourceException {

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

        var id = contentService.startPostCreateSession(token, communityId, caption, description);

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

        contentService.addContentToSession(token, sessionId, content);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.add-content.messages.post"));
    }

    @Deprecated
    @PostMapping(value = "${content.service.endpoints.create-post.path}")
    public ResponseEntity<?> createPost(JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestPart("image") MultipartFile file0,
                                        @RequestParam("caption") String caption,
                                        @RequestParam("description") String description,
                                        @RequestParam("contentType") PostContentType type,
                                        @RequestParam("count") Optional<Integer> count)
            throws BadRequestException, ValidationException, IOException, ResourceException {

        contentService.createPost(token, communityId, new CreatePostRequestBody(
                caption,
                description,
                type,
                1
        ), file0);
        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.create-post.messages.post"));
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
        var comments = contentService.getPostComments(postId);

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.get"),
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
