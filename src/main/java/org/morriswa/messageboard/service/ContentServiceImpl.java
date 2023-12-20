package org.morriswa.messageboard.service;

import org.morriswa.messageboard.model.DraftBody;
import org.morriswa.messageboard.dao.CommentDao;
import org.morriswa.messageboard.dao.PostDao;
import org.morriswa.messageboard.dao.PostDraftDao;
import org.morriswa.messageboard.dao.ResourceDao;
import org.morriswa.messageboard.model.PostWithCommunityInfo;
import org.morriswa.messageboard.enumerated.ModerationLevel;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.ResourceException;
import org.morriswa.messageboard.model.*;
import org.morriswa.messageboard.enumerated.PostContentType;
import org.morriswa.messageboard.enumerated.Vote;
import org.morriswa.messageboard.model.CommentRequest;
import org.morriswa.messageboard.model.CreatePostRequest;
import org.morriswa.messageboard.model.UploadImageRequest;
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

@Service
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
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.communityId());

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
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.communityId());

        final CommentRequest newComment= CommentRequest.buildSubCommentRequest(
                userId,
                postId,
                parentCommentId,
                comment);

        validator.validate(newComment);

        comments.createNewComment(newComment);
    }

    @Override
    public int voteOnPost(JwtAuthenticationToken token, Long postId, Vote vote) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.communityId());

        return posts.vote(userId, postId, vote);
    }

    @Override
    public int voteOnComment(JwtAuthenticationToken token, Long postId, Long commentId, Vote vote) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.verifyUserCanPostInCommunityOrThrow(userId, post.communityId());

        return comments.vote(userId, postId, commentId, vote);
    }

    @Override
    public UUID createPostDraft(JwtAuthenticationToken token, Long communityId, DraftBody draft) throws Exception {
        var userId = userProfileService.authenticate(token);

        validator.validate(draft);

        var newResource = new Resource();

        resources.createNewPostResource(newResource);

        var newDraftId = UUID.randomUUID();

        drafts.create(newDraftId, userId, communityId, newResource.getId(), draft);

        return newDraftId;
    }

    @Override
    public void editPostDraft(JwtAuthenticationToken token, UUID draftId, DraftBody draft) throws Exception {
        var userId = userProfileService.authenticate(token);

        validator.validateNonNull(draft);
        validator.validate(draft);

        drafts.edit(userId, draftId, draft);
    }

    @Override
    public void addContentToDraft(JwtAuthenticationToken token, UUID draftId, MultipartFile file) throws Exception {
        userProfileService.authenticate(token);

        Objects.requireNonNull(file.getContentType());

        var uploadRequest = new UploadImageRequest(
                file.getBytes(),
                blobTypeToImageFormat(file.getContentType())
        );

        validator.validate(uploadRequest);

        var session = drafts.getDraft(draftId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-session")));

        var resource = resources.findResourceByResourceId(session.resourceId())
            .orElseThrow(
                ()->new ResourceException(
                        String.format(
                            e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                            session.resourceId()))
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
    public PostDraft.Response getPostDraft(JwtAuthenticationToken token, UUID draftId) throws Exception {
        userProfileService.authenticate(token);

        var session = drafts.getDraft(draftId)
                .orElseThrow(()->new BadRequestException(
                        e.getRequiredProperty("content.service.errors.cannot-locate-session")));

        var resource = resources.findResourceByResourceId(session.resourceId())
                .orElseThrow(
                        ()->new ResourceException(
                                String.format(
                                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                                        session.resourceId()))
                );

        return new PostDraft.Response(session,
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

        var resource = resources.findResourceByResourceId(session.resourceId())
            .orElseThrow(
                ()->new ResourceException(
                    String.format(
                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                        session.resourceId()))
            );

        var newPost = new CreatePostRequest(userId,
                session.communityId(),
                session.caption(),
                session.description(),
                getDraftType(resource.getResources().size()),
                session.resourceId());

        validator.validate(newPost);

        posts.createNewPost(newPost);

        drafts.clearUsersDrafts(userId);
    }

    @Override
    public Post.PostCommentResponse retrievePostDetails(JwtAuthenticationToken token, Long postId) throws Exception {
        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        var resourceEntity = resources.findResourceByResourceId(post.resourceId())
                .orElseThrow(()->new ResourceException(
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                                post.resourceId())));
        var resourceUrls = new ArrayList<URL>(){{
            for (UUID resource : resourceEntity.getResources())
                add(content.retrieveImageResource(resource));
        }};

        var comments = getComments(post.postId());

        return new Post.PostCommentResponse(post, resourceUrls, comments);
    }

    @Override
    public void deletePost(JwtAuthenticationToken token, Long postId) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                    e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.assertUserHasPrivilegeInCommunity(userId, ModerationLevel.CONTENT_MOD, post.communityId());

        var resource = resources.findResourceByResourceId(post.resourceId())
                .orElseThrow(()->new ResourceException(String.format(
                        e.getRequiredProperty("content.service.errors.cannot-locate-resource"),
                        post.resourceId())));

        // delete all stored content
        content.deleteResource(resource);
        // delete resource
        resources.deleteResource(post.resourceId());
        // delete all post comments (inc. subcomments)
        comments.deletePostComments(post.postId());
        // finally delete post
        posts.deletePost(post.postId());
    }

    @Override
    public void deleteComment(JwtAuthenticationToken token, Long postId, Long commentId) throws Exception {
        var userId = userProfileService.authenticate(token);

        var post = posts.findPostByPostId(postId)
                .orElseThrow(()->new BadRequestException(
                        String.format(
                                e.getRequiredProperty("content.service.errors.cannot-locate-post"),
                                postId
                        )
                ));

        communityService.assertUserHasPrivilegeInCommunity(userId, ModerationLevel.COMMENT_MOD, post.communityId());

        // delete requested post comment and children
        comments.deleteCommentAndChildren(post.postId(), commentId);
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
    public List<Post.Response> getFeedForCommunity(Long communityId) throws Exception {

        var response = new ArrayList<Post.Response>();

        var allCommunityPosts = posts.findAllPostsByCommunityId(communityId);

        for (Post post : allCommunityPosts) {
            var resourceEntity = resources.findResourceByResourceId(post.resourceId())
                    .orElseThrow();

            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getResources())
                    add(content.retrieveImageResource(resource));
            }};

            response.add(new Post.Response(post, resourceUrls));
        }

        return response.stream().sorted(
                Comparator.comparing(o -> ((Post.Response) o).post().dateCreated()).reversed()
        ).toList();
    }

    @Override
    public List<Post.PostCommunityResponse> getRecentPosts() throws Exception {
        var recentPosts = posts.getMostRecent();

        var response = new ArrayList<Post.PostCommunityResponse>(10);

        for (PostWithCommunityInfo post : recentPosts) {
//            var community = communityService.getAllCommunityInfo(post.getCommunityId());
            var resourceEntity = resources.findResourceByResourceId(post.resourceId())
                    .orElseThrow();

            var communityIcon = communityService.getIcon(post.communityId());
            var resourceUrls = new ArrayList<URL>(){{
                for (UUID resource : resourceEntity.getResources())
                    add(content.retrieveImageResource(resource));
            }};

            response.add(new Post.PostCommunityResponse(post, resourceUrls, communityIcon));
        }

        return response;
    }
}
