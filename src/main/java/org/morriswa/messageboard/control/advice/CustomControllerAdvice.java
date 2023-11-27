package org.morriswa.messageboard.control.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.NoRegisteredUserException;
import org.morriswa.messageboard.exception.PermissionsException;
import org.morriswa.messageboard.exception.ValidationException;
import org.morriswa.messageboard.util.HttpResponseFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice @Slf4j
public class CustomControllerAdvice {
    private final Environment env;
    private final HttpResponseFactoryImpl responseFactory;

    @Autowired
    public CustomControllerAdvice(final Environment env, HttpResponseFactoryImpl responseFactory) {
        this.env = env;
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler({Exception.class}) // Catch any and all unhandled exceptions thrown in this controller
    public ResponseEntity<?> internalServerError(Exception e, WebRequest r) {
        log.error("Encountered 500 error: ", e);

        // and return a 500 with as much relevant information as they deserve
        return responseFactory.error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            e.getClass().getSimpleName(),
            e.getMessage());
    }

    @ExceptionHandler({ // catch...
            ConstraintViolationException.class, // Poorly formatted Objects and
            BadRequestException.class, // Bad Requests,
            NoRegisteredUserException.class // silly users
    }) // in this controller...
    public ResponseEntity<?> badRequest(Exception e, WebRequest r) {
        // and assume user fault [400]
        return responseFactory.error(
                        HttpStatus.BAD_REQUEST,
                        e.getClass().getSimpleName(),
                        e.getMessage());
    }

    @ExceptionHandler({ // catch...
            ValidationException.class // Custom Validator exceptions
    }) // in this controller...
    public ResponseEntity<?> handleValidationExceptions(Exception e, WebRequest r) {

        ValidationException v = (ValidationException) e;

        // and assume user fault [400]
        return responseFactory.error(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                env.getRequiredProperty("common.service.errors.validation-exception-thrown"),
                v.getValidationErrors());
    }

    @ExceptionHandler({ // catch...
            PermissionsException.class // Custom Validator exceptions
    }) // in this controller...
    public ResponseEntity<?> handlePermissionExceptions(Exception e, WebRequest r) {

        PermissionsException v = (PermissionsException) e;

        // and assume user fault [403]
        return responseFactory.error(
                HttpStatus.FORBIDDEN,
                e.getClass().getSimpleName(),
                env.getRequiredProperty("common.service.errors.permissions-exception-thrown"),
                v.getPermissionViolations());
    }

}
