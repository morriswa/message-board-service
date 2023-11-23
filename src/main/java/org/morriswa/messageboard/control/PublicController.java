package org.morriswa.messageboard.control;

import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.service.ContentService;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController @CrossOrigin @Slf4j
public class PublicController {
    private final Environment e;
    private final ContentService contentService;
    private final HttpResponseFactoryImpl responseFactory;


    @Autowired
    public PublicController(Environment e, ContentService contentService, HttpResponseFactoryImpl responseFactory) {
        this.e = e;
        this.contentService = contentService;
        this.responseFactory = responseFactory;
    }

    @GetMapping(path = "${common.service.endpoints.health.path}")
    public ResponseEntity<?> healthCheckup()
    {
        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("common.service.endpoints.health.messages.get"));
    }

    @GetMapping(path = "${server.path}${common.service.endpoints.health.path}")
    public ResponseEntity<?> secureHealthCheckup(JwtAuthenticationToken token)
    {
        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getRequiredProperty("common.service.endpoints.health.messages.get"),
                token);
    }

    @GetMapping("${common.service.endpoints.preferences.path}")
    public ResponseEntity<?> getApplicationPreferences() {
        var prefs = new HashMap<String, Object>(){{
            put("DISPLAY_NAME_MAX",
                    e.getRequiredProperty("user-profile.service.rules.display-name.max-length"));
            put("DISPLAY_NAME_MIN",
                    e.getRequiredProperty("user-profile.service.rules.display-name.min-length"));
            put("DISPLAY_NAME_PATTERN",
                    e.getRequiredProperty("user-profile.service.rules.display-name.regexp"));

            put("COMMUNITY_REF_MAX",
                    e.getRequiredProperty("community.service.rules.community-ref.max-length"));
            put("COMMUNITY_REF_MIN",
                    e.getRequiredProperty("community.service.rules.community-ref.min-length"));
            put("COMMUNITY_REF_PATTERN",
                    e.getRequiredProperty("community.service.rules.community-ref.regexp"));

            put("COMMUNITY_NAME_MAX",
                    e.getRequiredProperty("community.service.rules.display-name.max-length"));
            put("COMMUNITY_NAME_MIN",
                    e.getRequiredProperty("community.service.rules.display-name.min-length"));
        }};

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("common.service.endpoints.preferences.messages.get"),
                prefs);
    }

    @GetMapping("${common.service.endpoints.recent-post-feed.path}")
    public ResponseEntity<?> getRecentPosts() throws Exception {

        var feed = contentService.getRecentPosts();

        return responseFactory.getResponse(
                HttpStatus.OK,
                e.getProperty("common.service.endpoints.recent-post-feed.messages.get"),
                feed);
    }
}
