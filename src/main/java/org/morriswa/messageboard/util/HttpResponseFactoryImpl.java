package org.morriswa.messageboard.util;

import lombok.*;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.GregorianCalendar;

@Component
public class HttpResponseFactoryImpl implements HttpResponseFactory {
    private final String APPLICATION_NAME;
    private final String APPLICATION_VERSION;

    @Getter
    public static class DefaultResponse<T> {
        private final String message;
        private final GregorianCalendar timestamp;
        private final String applicationName;
        private final String version;
        private final T payload;

        public DefaultResponse(String message, String applicationName, String version) {
            this.message = message;
            this.timestamp = new GregorianCalendar();
            this.applicationName = applicationName;
            this.version = version;
            this.payload = null;
        };

        public DefaultResponse(String message, String applicationName, String version, T payload) {
            this.message = message;
            this.timestamp = new GregorianCalendar();
            this.applicationName = applicationName;
            this.version = version;
            this.payload = payload;
        };
    }

    /**
     * Default Response to send when an Exception needs to be returned
     */
    @Getter
    public static class DefaultErrorResponse {
        private final String error;
        private final String description;
        private final GregorianCalendar timestamp;
        private final String applicationName;
        private final String version;
        private final Object stack;

        public DefaultErrorResponse(String error, String description, String applicationName, String version) {
            this.error = error;
            this.description = description;
            this.timestamp = new GregorianCalendar();
            this.applicationName = applicationName;
            this.version = version;
            this.stack = null;
        }

        public DefaultErrorResponse(String error, String description, String applicationName, String version, Object stack) {
            this.error = error;
            this.description = description;
            this.timestamp = new GregorianCalendar();
            this.applicationName = applicationName;
            this.version = version;
            this.stack = stack;
        }
    }

    public HttpResponseFactoryImpl(Environment e) {
        this.APPLICATION_NAME = e.getRequiredProperty("maven-props.name");
        this.APPLICATION_VERSION = e.getRequiredProperty("maven-props.version");
    }

    public ResponseEntity<?> getResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new DefaultResponse<>(message, APPLICATION_NAME, APPLICATION_VERSION));
    }

    public ResponseEntity<?> getResponse(HttpStatus status, String message, Object payload) {
        return ResponseEntity
                .status(status)
                .body(new DefaultResponse<>(message,APPLICATION_NAME, APPLICATION_VERSION, payload));
    }

    public ResponseEntity<?> getErrorResponse(HttpStatus status, String message, String description) {
        return ResponseEntity
                .status(status)
                .body(new DefaultErrorResponse(message, description, APPLICATION_NAME, APPLICATION_VERSION));
    }

    public ResponseEntity<?> getErrorResponse(HttpStatus status, String message, String description, Object stack) {
        return ResponseEntity
                .status(status)
                .body(new DefaultErrorResponse(message, description, APPLICATION_NAME, APPLICATION_VERSION, stack));
    }
}
