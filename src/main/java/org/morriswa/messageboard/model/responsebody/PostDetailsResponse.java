package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.entity.Comment;

import java.util.List;

@Getter @AllArgsConstructor
public class PostDetailsResponse {
    private final PostResponse post;
    private final List<Comment> comments;
}
