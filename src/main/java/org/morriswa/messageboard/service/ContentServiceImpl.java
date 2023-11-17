package org.morriswa.messageboard.service;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.morriswa.messageboard.dao.CommentDao;
import org.morriswa.messageboard.dao.PostDao;
import org.morriswa.messageboard.dao.PostSessionDao;
import org.morriswa.messageboard.dao.ResourceDao;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.model.PostContentType;
import org.morriswa.messageboard.model.PostDraft;
import org.morriswa.messageboard.model.Vote;
import org.morriswa.messageboard.model.entity.Comment;
import org.morriswa.messageboard.model.entity.Post;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.validatedrequest.CommentRequest;
import org.morriswa.messageboard.model.requestbody.CreatePostRequestBody;
import org.morriswa.messageboard.model.validatedrequest.UploadImageRequest;
import org.morriswa.messageboard.model.responsebody.PostResponse;
import org.morriswa.messageboard.model.validatedrequest.CreatePostRequest;
import org.morriswa.messageboard.model.entity.Resource;
import org.morriswa.messageboard.stores.ImageStore;
import org.morriswa.messageboard.validation.ContentServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

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
                              ResourceDao resources, CommentDao commentRepo, PostSessionDao sessions) {
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

//        resourceRepo.createNewPostResource(newResource);

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

//            case PHOTO_GALLERY -> {
//                final int imagesToUpload = request.getCount();
//
//                if (imagesToUpload>10) throw new BadRequestException(
//                        e.getRequiredProperty("content.service.errors.too-many-images")
//                );
//
//                var generatedSource = new ArrayList<UUID>();
//
//                for (int i = 0; i < imagesToUpload; i++) {
//                    UUID newResourceUUID = i == 0 ? newResource.getId() : UUID.randomUUID();
//
//                    generatedSource.add(newResourceUUID);
//
//                    var uploadRequest = new UploadImageRequest(
//                            files[i].getBytes(),
//                            blobTypeToImageFormat(Objects.requireNonNull(files[i].getContentType()))
//                    );
//
//                    validator.validateImageRequestOrThrow(uploadRequest);
//
//                    imageStore.uploadIndividualImage(newResourceUUID, uploadRequest);
//                }
//
//                newResource.setList(generatedSource.subList(1, generatedSource.size()));
//                resources.createNewPostResource(newResource);
//            }

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

        final CommentRequest newComment= CommentRequest.buildCommentRequest(
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
    public void addContentToSession(JwtAuthenticationToken token, UUID sessionToken, MultipartFile file) throws BadRequestException, IOException, ValidationException, ResourceException {
        var userId = userProfileService.authenticate(token);

        var uploadRequest = new UploadImageRequest(
                file.getBytes(),
                blobTypeToImageFormat(Objects.requireNonNull(file.getContentType()))
        );

        validator.validateImageRequestOrThrow(uploadRequest);

        var session = sessions.getSession(sessionToken);

        var resource = resources.findResourceByResourceId(session.getResourceId()).orElseThrow();

        if (resource.getResources().size() >= 10)
            throw new RuntimeException();

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
        var userId = userProfileService.authenticate(token);

        var session = sessions.getSession(sessionToken);

        var resource = resources.findResourceByResourceId(session.getResourceId()).orElseThrow();

        final PostDraft response = new PostDraft(
                session.getSessionId(),
                session.getUserId(),
                session.getCommunityId(),
                session.getCaption(),
                session.getDescription(),
                resource.getResources().size()>1? PostContentType.PHOTO_GALLERY: PostContentType.PHOTO,
                new ArrayList<URL>(){{
                        for (UUID resource : resource.getResources())
                            add(imageStore.retrieveImageResource(resource));
                    }}
        );
        return response;
    }

    @Override
    public List<Comment> getComments(Long postId) {
        return commentRepo.findAllCommentsByPostId(postId);
    }

    @Override
    public List<Comment> getFullCommentMapForPost(Long postId) {
        var retrievedComments = commentRepo.findAllCommentsByPostId(postId);
//
//        List<CommentResponse> comments = new ArrayList<>();
//
//        List<CommentResponse> subComments = new ArrayList<>();
//
//        for (CommentRequest commentRequest : retrievedComments) {
//            if (commentRequest.getParentCommentId()==null)
//                comments.add(new CommentResponse(commentRequest));
//            else subComments.add(new CommentResponse(commentRequest));
//        }
//
//        // TODO map all subComments to Comments

        return retrievedComments;
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
