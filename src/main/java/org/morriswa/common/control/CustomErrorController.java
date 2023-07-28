package org.morriswa.common.control;

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
    @RequestMapping(path = "error")
    public ResponseEntity<?> ohNoErr404()
    {
        return ResponseEntity.status(404).body("Oh no! That page was not found :(");
    }
}
