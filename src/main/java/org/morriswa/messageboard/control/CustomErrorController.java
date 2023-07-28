package org.morriswa.messageboard.control;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generic Error Controller for all MorrisWA Web Services
 */
@RestController
public class CustomErrorController implements ErrorController
{
    @Value("${common.service.endpoints.error.messages.get}")
    private String errorMessage;
    @RequestMapping(path = "${common.service.endpoints.error.path}")
    public ResponseEntity<?> ohNoErr404()
    {
        return ResponseEntity.status(404).body(errorMessage);
    }
}
