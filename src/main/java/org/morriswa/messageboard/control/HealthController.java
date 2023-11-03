package org.morriswa.messageboard.control;

import org.morriswa.messageboard.model.responsebody.DefaultResponse;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${common.service.endpoints.health.messages.get}")
    private String successMessage;

    @GetMapping(path = "${common.service.endpoints.health.path}")
    public ResponseEntity<?> healthCheckup()
    {
        var response = new DefaultResponse<>(successMessage);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "${server.path}${common.service.endpoints.health.path}")
    public ResponseEntity<?> authHealthCheckup(JwtAuthenticationToken token)
    {
        var response = new DefaultResponse<>(successMessage,token);

        return ResponseEntity.ok().body(response);
    }
}
