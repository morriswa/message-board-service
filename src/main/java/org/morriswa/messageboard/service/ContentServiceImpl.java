package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.dao.CommentDao;
import org.morriswa.messageboard.dao.PostDao;
import org.morriswa.messageboard.dao.PostDraftDao;
import org.morriswa.messageboard.dao.ResourceDao;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.entity.Post;
import org.morriswa.messageboard.model.entity.Resource;
import org.morriswa.messageboard.model.enumerated.PostContentType;
import org.morriswa.messageboard.model.enumerated.Vote;
import org.morriswa.messageboard.model.responsebody.PostCommunityResponse;
import org.morriswa.messageboard.model.responsebody.PostDetailsResponse;
import org.morriswa.messageboard.model.responsebody.PostDraftResponse;
import org.morriswa.messageboard.model.responsebody.PostUserResponse;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.validatedrequest.CreatePostRequest;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.store.ContentStore;
import org.morriswa.messageboard.validation.ContentServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.blobTypeToImageFormat;

@Service @Slf4j
public class ContentServiceImpl implements ContentService {
    // application context
    private final Environment e;
    // required validators
    private final ContentServiceValidator validator;
    // required services
    private final CommunityService communityService;
    private final UserProfileService userProfileService;
    // required database
    private final PostDao posts;
    private final ResourceDao resources;
    private final CommentDao comments;
    private final PostDraftDao drafts;
    // required s3 stores
    private final ContentStore content;


    @Autowired
    public ContentServiceImpl(Environment e,
                              ContentServiceValidator validator,
                              CommunityService communityService,
                              UserProfileService userProfileService,
                              ContentStore content,
                              PostDao posts,
                              ResourceDao resources,
                              CommentDao comments,
                              PostDraftDao drafts) {
        this.e = e;
        this.validator = validator;
        this.communityService = communityService;
        this.userProfileService = userProfileService;
        this.content = content;
        this.posts = posts;
        this.resources = resources;
        this.comments = comments;
        this.drafts = drafts;
    }

    @Override
    public void leaveComment(JwtAuthenticationToken token, Long postId, String comment) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        final CommentRequest newComment = CommentRequest.buildCommentRequest(
                    userId,
                    postId,
                    comment);

        validator.validate(newComment);

        comments.createNewComment(newComment);
    }

    @Override
    public void leaveComment(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        final CommentRequest newComment= CommentRequest.buildSubCommentRequest(
                userId,
                postId,
                parentCommentId,
                comment);

        validator.validateBeanOrThrow(newComment);

        comments.createNewComment(newComment);
    }

    @Override
    public int voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        return posts.vote(userId, postId, vote);
    }

    @Override
    public int voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        return comments.vote(userId, postId, commentId, vote);
    }

    @Override
    public UUID createPostDraft(JwtAuthenticationToken token, Long communityId, Optional<String> caption, Optional<String> description) throws Exception {
        var userId = userProfileService.authenticate(token);

        var newResource = new Resource();

        resources.createNewPostResource(newResource);

        var id = UUID.randomUUID();

        drafts.create(id, userId, communityId, newResource.getId(), caption, description);

        return id;
    }

    @Override
    public void editPostDraft(JwtAuthenticationToken token, UUID draftId, Optional<String> caption, Optional<String> description) throws Exception {
        var userId = userProfileService.authenticate(token);

        drafts.edit(userId, draftId, caption, description);
    }

    @Override
    public void addContentToDraft(JwtAuthenticationToken token, UUID draftId, MultipartFile file) throws Exception {
        var userId = userProfileService.authenticate(token);

        Objects.requireNonNull(file.getContentType());

        var uploadRequest = new UploadImageRequest(
                file.getBytes(),
                blobTypeToImageFormat(file.getContentType())
        );

        validator.validate(uploadRequest);

        var session = drafts.getDraft(draftId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-session")));

        var resource = resources.findResourceByResourceId(session.getResourceId())
            .orElseThrow(
                ()->new ResourceException(
                        String.format(
                            e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                            session.getResourceId()))
            );

        final int maxAllowedResources = Integer.parseInt(
                e.getRequiredProperty("content.service.rules.max-allowed-content")
        );

        if (resource.getResources().size() >= maxAllowedResources)
            throw new ResourceException(
                    String.format(e.getRequiredProperty("content.service.errors.too-many-images"),
                            maxAllowedResources));

        final UUID newImageTag = UUID.randomUUID();

        content.uploadIndividualImage(
                newImageTag,
                uploadRequest
        );

        resource.add(newImageTag);

        resources.updateResource(resource);
    }

    @Override
    public PostDraftResponse getPostDraft(JwtAuthenticationToken token, UUID draftId) throws Exception {
        userProfileService.authenticate(token);

        var session = drafts.getDraft(draftId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-session")));

        var resource = resources.findResourceByResourceId(session.getResourceId())
                .orElseThrow(
                        ()->new ResourceException(
                                String.format(
                                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                                        session.getResourceId()))
                );

        return new PostDraftResponse(
                session.getSessionId(),
                session.getUserId(),
                session.getCommunityId(),
                session.getCaption(),
                session.getDescription(),
                getDraftType(resource.getResources().size()),
                new ArrayList<>() {{
                    for (UUID resource1 : resource.getResources())
                        add(content.retrieveImageResource(resource1));
                }}
        );
    }

    PostContentType getDraftType(int size) {
        if (size > 1) return PostContentType.PHOTO_GALLERY;
        else if (size == 1) return PostContentType.PHOTO;
        else if (size == 0) return PostContentType.TEXT;
        return PostContentType.TEXT;
    }

    @Override
    public void createPostFromDraft(JwtAuthenticationToken token, UUID draftId) throws Exception {
        var userId = userProfileService.authenticate(token);

        var session = drafts.getDraft(draftId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-session")));;

        var resource = resources.findResourceByResourceId(session.getResourceId())
            .orElseThrow(
                ()->new ResourceException(
                    String.format(
                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                        session.getResourceId()))
            );

        var newPost = new CreatePostRequest(userId,
                session.getCommunityId(),
                session.getCaption(),
                session.getDescription(),
                getDraftType(resource.getResources().size()),
                session.getResourceId());

        validator.validateBeanOrThrow(newPost);

        posts.createNewPost(newPost);

        drafts.clearUsersDrafts(userId);
    }

    @Override
    public PostDetailsResponse retrievePostDetails(JwtAuthenticationToken token, Long postId) throws Exception {
        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException("TODO Bad!"));

        var user = userProfileService.getUserProfile(post.getUserId());

        var resourceEntity = resources.findResourceByResourceId(post.getResourceId())
                .orElseThrow();
        var resourceUrls = new ArrayList<URL>(){{
            for (UUID resource : resourceEntity.getResources())
                add(content.retrieveImageResource(resource));
        }};

        var postResponseWithUserData = new PostUserResponse(post, user, resourceUrls);

        var comments = getComments(post.getPostId());

        return new PostDetailsResponse(postResponseWithUserData, comments);
    }

    @Override
    public List<Comment> getComments(Long postId) {
        return comments.findComments(postId);
    }

    @Override
    public List<Comment> getComments(Long postId, Long parentId) {
        return comments.findComments(postId, parentId);
    }

    @Override
    public List<PostUserResponse> getFeedForCommunity(Long communityId) throws Exception {

        var response = new ArrayList<PostUserResponse>();

        var allCommunityPosts = posts.findAllPostsByCommunityId(communityId);

        for (Post post : allCommunityPosts) {
            var user = userProfileService.getUserProfile(post.getUserId());
            var resourceEntity = resources.findResourceByResourceId(post.getResourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getResources())
                    add(content.retrieveImageResource(resource));
            }};

            response.add(new PostUserResponse(post, user, resourceUrls));
        }

        return response.stream().sorted(
                Comparator.comparing(PostUserResponse::getDateCreated).reversed()
        ).toList();
    }

    @Override
    public List<PostCommunityResponse> getRecentPosts() throws Exception {
        var recentPosts = posts.getMostRecent();

        var response = new ArrayList<PostCommunityResponse>(10);

        for (Post post : recentPosts) {
            var community = communityService.getAllCommunityInfo(post.getCommunityId());
            var resourceEntity = resources.findResourceByResourceId(post.getResourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getResources())
                    add(content.retrieveImageResource(resource));
            }};

            response.add(new PostCommunityResponse(post, community, resourceUrls));
        }

        return response;
    }
}
