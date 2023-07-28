package org.morriswa.messageboard.control;

import org.morriswa.common.model.DefaultResponse;
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
    @GetMapping(path = "health")
    public ResponseEntity<?> healthCheckup()
    {
        var response = new DefaultResponse<>("Hello! All is good on our side...");

        return ResponseEntity.ok().body(response);
    }

    @GetMapping(path = "v0/health")
    public ResponseEntity<?> authHealthCheckup(JwtAuthenticationToken token)
    {
        var response = new DefaultResponse<>("Hello! All is good on our side...",token);

        return ResponseEntity.ok().body(response);
    }
}
