package org.morriswa.messageboard.service;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.dao.CommentDao;
import org.morriswa.messageboard.dao.PostDao;
import org.morriswa.messageboard.dao.PostSessionDao;
import org.morriswa.messageboard.dao.ResourceDao;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.PostContentType;
import org.morriswa.messageboard.model.PostDraft;
import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.entity.Post;
import org.morriswa.messageboard.model.entity.Resource;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
import org.morriswa.messageboard.model.responsebody.PostResponse;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.validatedrequest.CreatePostRequest;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.stores.ImageStore;
import org.morriswa.messageboard.validation.ContentServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.morriswa.messageboard.util.Functions.blobTypeToImageFormat;

@Service @Slf4j
public class ContentServiceImpl implements ContentService {
    private final Environment e;
    private final ContentServiceValidator validator;
    private final CommunityService communityService;
    private final UserProfileService userProfileService;
    private final ImageStore imageStore;
    private final PostDao posts;
    private final ResourceDao resources;
    private final CommentDao commentRepo;
    private final PostSessionDao sessions;

    @Autowired
    public ContentServiceImpl(Environment e,
                              ContentServiceValidator validator,
                              CommunityService communityService,
                              UserProfileService userProfileService,
                              ImageStore imageStore,
                              PostDao posts,
                              ResourceDao resources,
                              CommentDao commentRepo,
                              PostSessionDao sessions) {
        this.e = e;
        this.validator = validator;
        this.communityService = communityService;
        this.userProfileService = userProfileService;
        this.imageStore = imageStore;
        this.posts = posts;
        this.resources = resources;
        this.commentRepo = commentRepo;
        this.sessions = sessions;
    }


    @Override
    public void createPost(JwtAuthenticationToken token, Long communityId, CreatePostRequestBody request, MultipartFile... files) throws BadRequestException, ValidationException, IOException, ResourceException {

        var userId = userProfileService.authenticate(token);

        communityService.verifyUserCanPostInCommunityOrThrow(userId, communityId);

        var newResource = new Resource();

        if (request.getCount() != files.length)
            throw new BadRequestException(e.getRequiredProperty(
                    "content.service.errors.wrong-number-of-files"
            ));

        switch (request.getContentType()) {
            case PHOTO -> {
                var uploadRequest = new UploadImageRequest(
                        files[0].getBytes(),
                        blobTypeToImageFormat(Objects.requireNonNull(files[0].getContentType()))
                );

                validator.validateImageRequestOrThrow(uploadRequest);

                final UUID newImageTag = UUID.randomUUID();

                imageStore.uploadIndividualImage(
                        newImageTag,
                        uploadRequest
                );

                newResource.add(newImageTag);
                resources.createNewPostResource(newResource);
            }

            default ->
                throw new BadRequestException(e.getRequiredProperty("content.service.errors.content-type-not-supported"));
        }

        var newPost = new CreatePostRequest(userId,
                communityId,
                request.getCaption(),
                request.getDescription(),
                request.getContentType(),
                newResource.getId());

        validator.validateBeanOrThrow(newPost);

        posts.createNewPost(newPost);
    }

    @Override
    public void addCommentToPost(JwtAuthenticationToken token, Long postId, String comment) throws BadRequestException {
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

        validator.validateBeanOrThrow(newComment);

        commentRepo.createNewComment(newComment);
    }

    @Override
    public void addCommentToPost(JwtAuthenticationToken token, Long postId, Long parentCommentId, String comment) throws BadRequestException {
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

        commentRepo.createNewComment(newComment);
    }

    @Override
    public void voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        posts.vote(userId, postId, vote);
    }

    @Override
    public void voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        commentRepo.vote(userId, postId, commentId, vote);
    }

    @Override
    public UUID startPostCreateSession(JwtAuthenticationToken token, Long communityId, Optional<String> caption, Optional<String> description) throws BadRequestException, ResourceException {
        var userId = userProfileService.authenticate(token);

        var newResource = new Resource();

        resources.createNewPostResource(newResource);

        var id = UUID.randomUUID();

        sessions.create(id, userId, communityId, newResource.getId(), caption, description);

        return id;
    }

    @Override
    public void editPostDraft(JwtAuthenticationToken token, UUID sessionToken, Optional<String> caption, Optional<String> description) throws BadRequestException {
        var userId = userProfileService.authenticate(token);

        sessions.edit(userId, sessionToken, caption, description);
    }

    @Override
    public void addContentToSession(JwtAuthenticationToken token, UUID sessionToken, MultipartFile file) throws BadRequestException, IOException, ValidationException, ResourceException {
        var userId = userProfileService.authenticate(token);

        Objects.requireNonNull(file.getContentType());

        var uploadRequest = new UploadImageRequest(
                file.getBytes(),
                blobTypeToImageFormat(file.getContentType())
        );

        validator.validateImageRequestOrThrow(uploadRequest);

        var session = sessions.getSession(sessionToken);

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

        imageStore.uploadIndividualImage(
                newImageTag,
                uploadRequest
        );

        resource.add(newImageTag);

        resources.updateResource(resource);
    }

    @Override
    public PostDraft getSession(JwtAuthenticationToken token, UUID sessionToken) throws BadRequestException, ResourceException {
        userProfileService.authenticate(token);

        var session = sessions.getSession(sessionToken);

        var resource = resources.findResourceByResourceId(session.getResourceId())
                .orElseThrow(
                        ()->new ResourceException(
                                String.format(
                                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                                        session.getResourceId()))
                );

        return new PostDraft(
                session.getSessionId(),
                session.getUserId(),
                session.getCommunityId(),
                session.getCaption(),
                session.getDescription(),
                getDraftType(resource.getResources().size()),
                new ArrayList<>() {{
                    for (UUID resource1 : resource.getResources())
                        add(imageStore.retrieveImageResource(resource1));
                }}
        );
    }

    PostContentType getDraftType(int size) {
        return size>1? PostContentType.PHOTO_GALLERY: PostContentType.PHOTO;
    }

    @Override
    public void postDraft(JwtAuthenticationToken token, UUID sessionToken) throws BadRequestException, ResourceException {
        var userId = userProfileService.authenticate(token);

        var session = sessions.getSession(sessionToken);

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

        sessions.clearUserSessions(userId);
    }

    @Override
    public List<Comment> getPostComments(Long postId) {
        return commentRepo.findAllCommentsByPostId(postId);
    }

    @Override
    public List<PostResponse> getFeedForCommunity(Long communityId) throws BadRequestException, ResourceException {

        var response = new ArrayList<PostResponse>();

        var allCommunityPosts = posts.findAllPostsByCommunityId(communityId);

        for (Post post : allCommunityPosts) {
            var user = userProfileService.getUserProfile(post.getUserId());
            var resourceEntity = resources.findResourceByResourceId(post.getResourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getResources())
                    add(imageStore.retrieveImageResource(resource));
            }};

            response.add(new PostResponse(post, user, resourceUrls));
        }

        return response.stream().sorted(
                Comparator.comparing(PostResponse::getDateCreated).reversed()
        ).toList();
    }
}
