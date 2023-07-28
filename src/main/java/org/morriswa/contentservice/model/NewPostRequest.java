package org.morriswa.contentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class NewPostRequest {
    private String authZeroId;
    private Long communityId;
    private String caption;
    private String description;
    private PostContentType contentType;
    private Object content;
}
