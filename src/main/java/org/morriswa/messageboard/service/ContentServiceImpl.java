package org.morriswa.messageboard.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.morriswa.messageboard.dao.CommentDao;
import org.morriswa.messageboard.dao.PostDao;
import org.morriswa.messageboard.dao.ResourceDao;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.model.Comment;
import org.morriswa.messageboard.model.CreatePostRequest;
import org.morriswa.messageboard.model.Resource;
import org.morriswa.messageboard.stores.ImageStore;
import org.morriswa.messageboard.validation.ContentServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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

    @Autowired
    public ContentServiceImpl(Environment e,
                              ContentServiceValidator validator,
                              CommunityService communityService,
                              UserProfileService userProfileService,
                              ImageStore imageStore,
                              PostDao posts,
                              ResourceDao resources, CommentDao commentRepo) {
        this.e = e;
        this.validator = validator;
        this.communityService = communityService;
        this.userProfileService = userProfileService;
        this.imageStore = imageStore;
        this.posts = posts;
        this.resources = resources;
        this.commentRepo = commentRepo;
    }


    @Override
    public void createPost(JwtAuthenticationToken token, Long communityId, NewPostRequest request) throws BadRequestException, IOException {

        var userId = userProfileService.authenticate(token);

        communityService.verifyUserCanPostInCommunityOrThrow(userId, communityId);

        var newResource = new Resource();

//        resourceRepo.createNewPostResource(newResource);

        switch (request.getContentType()) {
            case PHOTO -> {
                imageStore.uploadIndividualImage(
                        newResource.getResourceId(),
                        new UploadImageRequest(
                                (String) request.getContent().get("baseEncodedImage"),
                                (String) request.getContent().get("imageFormat")
                        ));

                resources.createNewPostResource(newResource);
            }

            case PHOTO_GALLERY -> {
                int imagesToUpload = (int) request.getContent().get("count");

                if (imagesToUpload>10) throw new BadRequestException(
                        e.getRequiredProperty("content.service.errors.too-many-images")
                );

                var generatedSource = new ArrayList<UUID>();

                for (int i = 0; i < imagesToUpload; i++) {
                    UUID newResourceUUID = i == 0 ? newResource.getResourceId() : UUID.randomUUID();

                    generatedSource.add(newResourceUUID);

                    imageStore.uploadIndividualImage(newResourceUUID,
                            new UploadImageRequest(
                                    (String) request.getContent().get("baseEncodedImage" + i),
                                    (String) request.getContent().get("imageFormat" + i)
                            ));
                }

                try {
                    newResource.setList(generatedSource);
                    resources.createNewPostResource(newResource);
                } catch (Exception e) {
                    throw new RuntimeException("naughty");
                }
            }

            default ->
                throw new BadRequestException(e.getRequiredProperty("content.service.errors.content-type-not-supported"));
        }

        var newPost = new CreatePostRequest(userId,
                communityId,
                request.getCaption(),
                request.getDescription(),
                request.getContentType(),
                newResource.getResourceId());

        validator.validateBeanOrThrow(newPost);

        posts.createNewPost(newPost);
    }

    @Override
    public void addCommentToPost(JwtAuthenticationToken token, NewCommentRequest request) throws BadRequestException {
        var userId = userProfileService.authenticateAndGetUserProfile(token).getUserId();

        var post = posts.findPostByPostId(request.getPostId())
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.getCommunityId());

        var newCommentBuilder = Comment.builder()
                .userId(userId)
                .postId(post.getPostId())
                .commentBody(request.getComment());

        if (request.getParentCommentId() != null)
            newCommentBuilder.parentCommentId(request.getParentCommentId());

        var newComment = newCommentBuilder.build();

        validator.validateBeanOrThrow(newComment);

        commentRepo.createNewComment(newComment);
    }

    @Override
    public List<CommentResponse> getFullCommentMapForPost(Long postId) {
        var retrievedComments = commentRepo.findAllCommentsByPostId(postId);

        List<CommentResponse> comments = new ArrayList<>();

        List<CommentResponse> subComments = new ArrayList<>();

        for (Comment comment : retrievedComments) {
            if (comment.getParentCommentId()==null)
                comments.add(new CommentResponse(comment));
            else subComments.add(new CommentResponse(comment));
        }

        // TODO map all subComments to Comments

        return comments;
    }

    @Override
    public List<PostResponse> getFeedForCommunity(Long communityId) throws BadRequestException {

        var response = new ArrayList<PostResponse>();

        var allCommunityPosts = posts.findAllPostsByCommunityId(communityId);

        for (Post post : allCommunityPosts) {
            var user = userProfileService.getUserProfile(post.getUserId());
            var resourceEntity = resources.findResourceByResourceId(post.getResourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getList())
                    add(imageStore.retrieveImageResource(resource));
            }};

            response.add(new PostResponse(post, user, resourceUrls));
        }

        return response;
    }
}
