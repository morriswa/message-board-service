package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data @AllArgsConstructor @NoArgsConstructor
public class NewPostRequest {
    private String caption;
    private String description;
    private PostContentType contentType;
    private Map<String,Object> content;
}
