package org.morriswa.messageboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * Generic Exception to throw when the server cannot complete a request
 */
@Getter
public class ValidationException extends Exception {

    private final List<ValidationError> validationErrors;

    public ValidationException(String msg, List<ValidationError> validationErrors) {
        super(msg);
        this.validationErrors = validationErrors;
    }
}