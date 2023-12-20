package org.morriswa.messageboard.exception;

import org.morriswa.messageboard.enumerated.RequestField;

import java.util.Collections;
import java.util.List;

/**
 * Validation Exception to throw when the server cannot complete a request due to user input error
 */
public class ValidationException extends Exception {

    /**
     * Creates an individual Validation Error to be thrown as part of Validation Exception
     *
     * @param field name of the request field that failed validation
     * @param rejectedValue value passed in request
     * @param message error message to be provided
     */
    public record ValidationError (
            String field,
            RequestField status,
            String rejectedValue,
            String message
    ) { }



    private final List<ValidationError> validationErrors;

    /**
     * Creates a Validation Exception when only one error needs to be returned
     *
     * @param problemField name of the request field that failed validation
     * @param problemValue value passed in request
     * @param errorMessage error message to be provided
     */
    public ValidationException(String problemField, RequestField required, String problemValue, String errorMessage) {
        super();
        this.validationErrors = Collections.singletonList(new ValidationError(
                problemField, required, problemValue, errorMessage
        ));
    }

    /**
     * Creates a Validation Exception when multiple errors need to be returned
     *
     * @param validationErrors to be included in response
     */
    public ValidationException(List<ValidationError> validationErrors) {
        super();
        this.validationErrors = validationErrors;
    }

    public List<ValidationError> getValidationErrors() {
        return this.validationErrors;
    }

}