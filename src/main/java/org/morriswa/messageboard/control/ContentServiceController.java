package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.Comment;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.model.PostCommentResponse;
import org.morriswa.messageboard.model.PostDraftResponse;
import org.morriswa.messageboard.model.PostResponse;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @GetMapping(value = "${content.service.endpoints.draft.path}")
    public ResponseEntity<?> getSession(
            JwtAuthenticationToken token,
            @PathVariable UUID draftId) throws Exception {

        PostDraftResponse draft = contentService.getPostDraft(token, draftId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.draft.messages.get"),
                draft);
    }

    @PostMapping(value = "${content.service.endpoints.draft.path}")
    public ResponseEntity<?> createPost(
            JwtAuthenticationToken token,
            @PathVariable UUID draftId)
            throws Exception {

        contentService.createPostFromDraft(token, draftId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.draft.messages.post")
        );
    }

    @PatchMapping(value = "${content.service.endpoints.draft.path}")
    public ResponseEntity<?> editPostDraft(
            JwtAuthenticationToken token,
            @PathVariable UUID draftId,
            @RequestParam Optional<String> caption,
            @RequestParam Optional<String> description)
            throws Exception {

        contentService.editPostDraft(token, draftId, caption, description);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.draft.messages.patch"));
    }

    @PostMapping(value = "${content.service.endpoints.create-draft.path}")
    public ResponseEntity<?> startPostSession(
                                        JwtAuthenticationToken token,
                                        @PathVariable Long communityId,
                                        @RequestParam Optional<String> caption,
                                        @RequestParam Optional<String> description)
            throws Exception {

        UUID id = contentService.createPostDraft(token, communityId, caption, description);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.create-draft.messages.post"),
                id);
    }

    @PostMapping(value = "${content.service.endpoints.add-content.path}")
    public ResponseEntity<?> addContent(
            JwtAuthenticationToken token,
            @PathVariable UUID draftId,
            @RequestPart MultipartFile content)
            throws Exception {

        contentService.addContentToDraft(token, draftId, content);

        return responseFactory.build(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.add-content.messages.post"));
    }

    @GetMapping("${content.service.endpoints.community-feed.path}")
    public ResponseEntity<?> getCommunityFeed(@PathVariable Long communityId) throws Exception {

        List<PostResponse> feed = contentService.getFeedForCommunity(communityId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getProperty("content.service.endpoints.community-feed.messages.get"),
                feed);
    }

    @GetMapping("${content.service.endpoints.post-details.path}")
    public ResponseEntity<?> getPostDetails(JwtAuthenticationToken token,
                                        @PathVariable Long postId) throws Exception {
        PostCommentResponse post = contentService.retrievePostDetails(token, postId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-details.messages.get"),
                post);
    }


    @PostMapping("${content.service.endpoints.post-voting.path}")
    public ResponseEntity<?> voteOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @RequestParam Vote vote) throws Exception {
        int count = contentService.voteOnPost(token, postId, vote);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.post-voting.messages.post"),
                count);
    }

    @PostMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> leaveCommentOnPost(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @RequestBody String comment) throws Exception {

        contentService.leaveComment(token, postId, comment);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.post"));
    }

    @PostMapping("${content.service.endpoints.sub-comment.path}")
    public ResponseEntity<?> leaveSubComment(JwtAuthenticationToken token,
                                                @PathVariable Long postId,
                                                @PathVariable Long parentId,
                                                @RequestBody String comment) throws Exception {

        contentService.leaveComment(token, postId, parentId, comment);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.sub-comment.messages.post"));
    }

    @GetMapping("${content.service.endpoints.comment.path}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) throws BadRequestException {
        List<Comment> comments = contentService.getComments(postId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment.messages.get"),
                comments);
    }

    @GetMapping("${content.service.endpoints.sub-comment.path}")
    public ResponseEntity<?> getSubComments(@PathVariable Long postId,
                                            @PathVariable Long parentId) throws BadRequestException {
        var comments = contentService.getComments(postId, parentId);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.sub-comment.messages.get"),
                comments);
    }

    @PostMapping("${content.service.endpoints.comment-voting.path}")
    public ResponseEntity<?> voteOnComment(JwtAuthenticationToken token,
                                        @PathVariable Long postId,
                                        @PathVariable Long commentId,
                                        @RequestParam Vote vote) throws Exception {
        int count = contentService.voteOnComment(token, postId, commentId, vote);

        return responseFactory.build(
                HttpStatus.OK,
                e.getRequiredProperty("content.service.endpoints.comment-voting.messages.post"),
                count);
    }
}
