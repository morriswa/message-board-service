package org.morriswa.messageboard.control;

import org.morriswa.messageboard.util.HttpResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("${common.service.endpoints.developer.path}")
public class DeveloperToolsController {

    private final Environment e;
    private final HttpResponseFactory responses;
//    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    public DeveloperToolsController(Environment e, HttpResponseFactory responses) {
        this.e = e;
        this.responses = responses;
    }

    @GetMapping
    public ResponseEntity<?> getDeveloperInfo(JwtAuthenticationToken token) {


        if (    !token.getToken().getClaimAsStringList("permissions")
                .contains("org.morriswa.messageboard:develop"))
            return responses.error(HttpStatus.FORBIDDEN, "not a developer","silly");

        return responses.build(HttpStatus.OK,e.getRequiredProperty("common.service.endpoints.developer.messages.get"));

    }
}
