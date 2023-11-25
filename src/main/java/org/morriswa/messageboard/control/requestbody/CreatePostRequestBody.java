package org.morriswa.messageboard.control.requestbody;

import org.morriswa.messageboard.enumerated.PostContentType;

public record CreatePostRequestBody (
    String caption,
    String description,
    PostContentType contentType,
    int count
) { }