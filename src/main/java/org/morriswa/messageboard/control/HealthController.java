package org.morriswa.messageboard.control;

import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cross Origin Rest Controller for all Health related requests
 */
@RestController @CrossOrigin
public class HealthController {

    private final HttpResponseFactoryImpl responseFactory;

    @Value("${common.service.endpoints.health.messages.get}")
    private String successMessage;

    @Autowired
    public HealthController(HttpResponseFactoryImpl responseFactory) {
        this.responseFactory = responseFactory;
    }

    @GetMapping(path = "${common.service.endpoints.health.path}")
    public ResponseEntity<?> healthCheckup()
    {
        return responseFactory.getResponse(HttpStatus.OK, successMessage);
    }

    @GetMapping(path = "${server.path}${common.service.endpoints.health.path}")
    public ResponseEntity<?> authHealthCheckup(JwtAuthenticationToken token)
    {
        return responseFactory.getResponse(HttpStatus.OK, successMessage, token);
    }
}
