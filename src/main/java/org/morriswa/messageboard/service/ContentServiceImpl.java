package org.morriswa.messageboard.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.entity.Comment;
import org.morriswa.messageboard.entity.Post;
import org.morriswa.messageboard.entity.Resource;
import org.morriswa.messageboard.repo.CommentRepo;
import org.morriswa.messageboard.repo.PostRepo;
import org.morriswa.messageboard.repo.ResourceRepo;
import org.morriswa.messageboard.service.util.ImageResourceService;
import org.morriswa.messageboard.validation.ContentServiceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class ContentServiceImpl implements ContentService {
    private final Environment e;
    private final ContentServiceValidator validator;
    private final CommunityService communityService;
    private final UserProfileService userProfileService;
    private final ImageResourceService imageResourceService;
    private final PostRepo postRepo;
    private final ResourceRepo resourceRepo;
    private final CommentRepo commentRepo;

    @Autowired
    public ContentServiceImpl(Environment e,
                              ContentServiceValidator validator,
                              CommunityService communityService,
                              UserProfileService userProfileService,
                              ImageResourceService imageResourceService,
                              PostRepo postRepo,
                              ResourceRepo resourceRepo, CommentRepo commentRepo) {
        this.e = e;
        this.validator = validator;
        this.communityService = communityService;
        this.userProfileService = userProfileService;
        this.imageResourceService = imageResourceService;
        this.postRepo = postRepo;
        this.resourceRepo = resourceRepo;
        this.commentRepo = commentRepo;
    }


    @Override
    public void createPost(JwtAuthenticationToken token, Long communityId, NewPostRequest request) throws BadRequestException, IOException {

        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        communityService.verifyUserCanPostInCommunityOrThrow(userId, communityId);

        var newResource = new Resource();

        resourceRepo.save(newResource);

        switch (request.getContentType()) {
            case PHOTO ->
                imageResourceService.uploadImage(
                        newResource.getResourceId(),
                        new UploadImageRequest(
                                (String) request.getContent().get("baseEncodedImage"),
                                (String) request.getContent().get("imageFormat")
                        ));

            case PHOTO_GALLERY -> {
                int imagesToUpload = (int) request.getContent().get("count");

                if (imagesToUpload>10) throw new BadRequestException(
                        e.getRequiredProperty("content.service.errors.too-many-images")
                );

                var generatedSource = new ArrayList<UUID>();

                for (int i = 0; i < imagesToUpload; i++) {
                    UUID newResourceUUID = i == 0 ? newResource.getResourceId() : UUID.randomUUID();

                    generatedSource.add(newResourceUUID);

                    imageResourceService.uploadImage(newResourceUUID,
                            new UploadImageRequest(
                                    (String) request.getContent().get("baseEncodedImage" + i),
                                    (String) request.getContent().get("imageFormat" + i)
                            ));
                }

                try {
                    newResource.setList(generatedSource);
                    resourceRepo.save(newResource);
                } catch (Exception e) {
                    throw new RuntimeException("naughty");
                }


            }

            default ->
                throw new BadRequestException(e.getRequiredProperty("content.service.errors.content-type-not-supported"));
        }


        var newPost = new Post(userId,
                communityId,
                request.getCaption(),
                request.getDescription(),
                request.getContentType(),
                newResource.getResourceId());


        validator.validateBeanOrThrow(newPost);

        postRepo.save(newPost);


    }

    @Override
    public void addCommentToPost(JwtAuthenticationToken token, NewCommentRequest request) throws BadRequestException {
        var userId = userProfileService.authenticateAndGetUserEntity(token).getUserId();

        var post = postRepo.findPostByPostId(request.getPostId())
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

        commentRepo.save(newComment);
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
    public List<PhotosPostResponse> getFeedForCommunity(Long communityId) throws BadRequestException {

        var response = new ArrayList<PhotosPostResponse>();

        var allCommunityPosts = postRepo.findAllPostsByCommunityId(communityId);

        for (Post post : allCommunityPosts) {
            var user = userProfileService.getUserProfile(post.getUserId());
            var resourceEntity = resourceRepo.findResourceByResourceId(post.getResourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getList())
                    add(imageResourceService.retrievedImageResource(resource));
            }};

            response.add(new PhotosPostResponse(post, user, resourceUrls));

        }

        return response;
    }
}
