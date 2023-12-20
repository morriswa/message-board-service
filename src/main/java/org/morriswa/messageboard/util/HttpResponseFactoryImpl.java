package org.morriswa.messageboard.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;

@Component
public class HttpResponseFactoryImpl implements HttpResponseFactory {
    private final String APPLICATION_NAME;
    private final String APPLICATION_VERSION;

    private record DefaultResponse<T> (
        String message,
        GregorianCalendar timestamp,
        String applicationName,
        String version,
        T payload
    ) { }

    /**
     * Default Response to send when an Exception needs to be returned
     */
    private record DefaultErrorResponse (
        String error,
        String description,
        GregorianCalendar timestamp,
        String applicationName,
        String version,
        Object stack
    ){

    }


    @Autowired
    public HttpResponseFactoryImpl(BuildProperties build) {
        this.APPLICATION_NAME = build.getName();
        this.APPLICATION_VERSION = build.getVersion();
    }

    public ResponseEntity<?> build(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new DefaultResponse<>(
                    message, new GregorianCalendar(), this.APPLICATION_NAME, this.APPLICATION_VERSION, null)
                );
    }

    public ResponseEntity<?> build(HttpStatus status, String message, Object payload) {
        return ResponseEntity
                .status(status)
                .body(new DefaultResponse<>(
                        message, new GregorianCalendar(), this.APPLICATION_NAME, this.APPLICATION_VERSION, payload
                ));
    }

    public ResponseEntity<?> error(HttpStatus status, String message, String description) {
        return ResponseEntity
                .status(status)
                .body(new DefaultErrorResponse(message, description, new GregorianCalendar(),
                        APPLICATION_NAME, APPLICATION_VERSION, null));
    }

    public ResponseEntity<?> error(HttpStatus status, String message, String description, Object stack) {
        return ResponseEntity
                .status(status)
                .body(new DefaultErrorResponse(message, description, new GregorianCalendar(),
                        APPLICATION_NAME, APPLICATION_VERSION, stack));
    }
}
