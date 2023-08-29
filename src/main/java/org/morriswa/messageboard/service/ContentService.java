package org.morriswa.messageboard.service;

import java.io.IOException;
import java.util.List;

import org.morriswa.messageboard.model.*;

public interface ContentService {
    void createPost(NewPostRequest request) throws BadRequestException, IOException;
    void addCommentToPost(NewCommentRequest request) throws BadRequestException;
    List<CommentResponse> getFullCommentMapForPost(Long postId);

    List<PhotosPostResponse> getFeedForCommunity(Long communityId) throws BadRequestException;
}
