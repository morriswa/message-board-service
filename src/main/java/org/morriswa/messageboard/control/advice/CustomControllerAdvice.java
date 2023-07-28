package org.morriswa.messageboard.control.advice;

import jakarta.validation.ConstraintViolationException;
import org.morriswa.messageboard.model.BadRequestException;
import org.morriswa.messageboard.model.DefaultErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.GregorianCalendar;

@ControllerAdvice
public class CustomControllerAdvice {
    @ExceptionHandler(Exception.class) // Catch any and all unhandled exceptions thrown in this controller
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

}
