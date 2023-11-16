package org.morriswa.messageboard.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor @Getter
public class Comment {
    private Long commentId;
    private UUID userId;
    private Long postId;
    private Long parentId;
    private String body;
}
