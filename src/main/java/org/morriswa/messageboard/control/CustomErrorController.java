package org.morriswa.messageboard.control;

import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
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

    private final HttpResponseFactoryImpl responseFactory;

    public CustomErrorController(HttpResponseFactoryImpl responseFactory) {
        this.responseFactory = responseFactory;
    }

    @RequestMapping(path = "${common.service.endpoints.error.path}")
    public ResponseEntity<?> ohNoErr404()
    {
        return responseFactory.getErrorResponse(HttpStatus.NOT_FOUND, "404" ,errorMessage);
    }
}
