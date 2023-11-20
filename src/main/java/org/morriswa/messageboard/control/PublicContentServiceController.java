package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @CrossOrigin @Slf4j
public class PublicContentServiceController {
    private final Environment e;
    private final ContentService contentService;
    private final HttpResponseFactoryImpl responseFactory;

    @Autowired
    public PublicContentServiceController(Environment e, ContentService contentService, HttpResponseFactoryImpl responseFactory) {
        this.e = e;
        this.contentService = contentService;
        this.responseFactory = responseFactory;
    }

    @GetMapping("${public-content.service.endpoints.recent-post-feed.path}")
    public ResponseEntity<?> getRecentPosts() throws Exception {

        var feed = contentService.getRecentPosts();

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("public-content.service.endpoints.recent-post-feed.messages.get"),
                feed);
    }

}
