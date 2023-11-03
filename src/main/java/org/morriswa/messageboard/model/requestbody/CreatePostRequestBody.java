package org.morriswa.messageboard.model.requestbody;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.morriswa.messageboard.model.PostContentType;

import java.util.Map;

@Getter @AllArgsConstructor
public class CreatePostRequestBody {
    private String caption;
    private String description;
    private PostContentType contentType;
    private Map<String,Object> content;
}
