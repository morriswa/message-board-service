package org.morriswa.messageboard.model.requestbody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.PostContentType;

@Getter @AllArgsConstructor
public class CreatePostRequestBody {
    private String caption;
    private String description;
    private PostContentType contentType;
    private int count;
}
