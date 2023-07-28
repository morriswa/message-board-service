package org.morriswa.contentservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.common.model.UploadImageRequest;
import org.morriswa.communityservice.service.CommunityService;
import org.morriswa.contentservice.entity.Comment;
import org.morriswa.contentservice.entity.Post;
import org.morriswa.contentservice.entity.Resource;
import org.morriswa.contentservice.model.CommentResponse;
import org.morriswa.contentservice.model.NewCommentRequest;
import org.morriswa.contentservice.model.NewPostRequest;
import org.morriswa.contentservice.repo.CommentRepo;
import org.morriswa.contentservice.repo.PostRepo;
import org.morriswa.contentservice.repo.ResourceRepo;
import org.morriswa.contentservice.service.util.ImageResourceService;
import org.morriswa.userprofileservice.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    public void createPost(NewPostRequest request) throws BadRequestException, IOException {

        var userId = userProfileService.getUserId(request.getAuthZeroId());

        if (!communityService.canUserPostInCommunity(userId, request.getCommunityId())) {
            throw new BadRequestException(
                    e.getRequiredProperty("content.service.errors.user-cannot-post")
            );
        }

        var newResource = new Resource();

        resourceRepo.save(newResource);

        switch (request.getContentType()) {
            case PHOTO:
                imageResourceService.uploadImage(newResource.getResourceId(), (UploadImageRequest) request.getContent());
                break;
        
            default:
                throw new BadRequestException(e.getRequiredProperty("content.service.errors.content-type-not-supported"));
        }


        var newPost = new Post(userId,
                request.getCommunityId(),
                request.getCaption(),
                request.getDescription(),
                request.getContentType(),
                newResource.getResourceId());


        validator.validateBeanOrThrow(newPost);

        postRepo.save(newPost);


    }

    @Override
    public void addCommentToPost(NewCommentRequest request) throws BadRequestException {
        var userId = userProfileService.getUserId(request.getAuthZeroId());

        var post = postRepo.findPostByPostId(request.getPostId())
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-post")
                ));

        if (!communityService.canUserPostInCommunity(userId, post.getCommunityId())) {
            throw new BadRequestException(
                    e.getRequiredProperty("content.service.errors.user-cannot-post")
            );
        }

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
}
