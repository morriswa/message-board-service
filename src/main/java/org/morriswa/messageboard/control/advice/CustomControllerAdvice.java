package org.morriswa.messageboard.control.advice;

import jakarta.validation.ConstraintViolationException;
import org.morriswa.messageboard.exception.BadRequestException;
import org.morriswa.messageboard.exception.InternalServerError;
import org.morriswa.messageboard.model.responsebody.DefaultErrorResponse;
import org.morriswa.messageboard.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.GregorianCalendar;

@ControllerAdvice
public class CustomControllerAdvice {
    private final Environment env;

    @Autowired
    public CustomControllerAdvice(final Environment env) {
        this.env = env;
    }

    @ExceptionHandler({Exception.class, InternalServerError.class}) // Catch any and all unhandled exceptions thrown in this controller
    public ResponseEntity<?> internalServerError(Exception e, WebRequest r) {
        var response = DefaultErrorResponse.builder()
                .error(e.getClass().getName())
                .message(e.getMessage())
                .timestamp(new GregorianCalendar())
                .build();
        // and return a 500 with as much relevant information as they deserve
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler({ // catch...
            ConstraintViolationException.class, // Poorly formatted Objects and
            BadRequestException.class // Bad Requests
    }) // in this controller...
    public ResponseEntity<?> badRequest(Exception e, WebRequest r) {
        var response = DefaultErrorResponse.builder()
                .error(e.getClass().getName())
                .message(e.getMessage())
                .timestamp(new GregorianCalendar())
                .build();
        // and assume user fault [400]
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({ // catch...
            ValidationException.class // Custom Validator exceptions
    }) // in this controller...
    public ResponseEntity<?> handleValidationExceptions(Exception e, WebRequest r) {

        ValidationException v = (ValidationException) e;

        var response = DefaultErrorResponse.builder()
                .error(e.getClass().getName())
                .stack(v.getValidationErrors())
                .message(env.getRequiredProperty("common.service.errors.validation-exception-thrown"))
                .timestamp(new GregorianCalendar())
                .build();
        // and assume user fault [400]
        return ResponseEntity.badRequest().body(response);
    }

}
