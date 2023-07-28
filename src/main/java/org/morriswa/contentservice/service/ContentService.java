package org.morriswa.contentservice.service;

import java.io.IOException;
import java.util.List;

import org.morriswa.common.model.BadRequestException;
import org.morriswa.contentservice.model.CommentResponse;
import org.morriswa.contentservice.model.NewCommentRequest;
import org.morriswa.contentservice.model.NewPostRequest;

public interface ContentService {
    void createPost(NewPostRequest request) throws BadRequestException, IOException;
    void addCommentToPost(NewCommentRequest request) throws BadRequestException;
    List<CommentResponse> getFullCommentMapForPost(Long postId);
}
