package org.morriswa.messageboard.model.responsebody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.morriswa.messageboard.model.enumerated.PostContentType;

import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

@Getter @AllArgsConstructor
public class PostResponse {
    private final Long postId;
    private final int vote;
    private final String caption;
    private final String description;
    private final PostContentType contentType;
    private final GregorianCalendar dateCreated;
    private final List<URL> resources;
}
