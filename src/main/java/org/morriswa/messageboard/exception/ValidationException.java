package org.morriswa.messageboard.exception;

import lombok.Getter;
import java.util.Collections;
import java.util.List;

/**
 * Validation Exception to throw when the server cannot complete a request due to user input error
 */
@Getter
public class ValidationException extends Exception {

    private final List<ValidationError> validationErrors;

    /**
     * Creates a Validation Exception when only one error needs to be returned
     *
     * @param problemField name of the request field that failed validation
     * @param problemValue value passed in request
     * @param errorMessage error message to be provided
     */
    public ValidationException(String problemField, String problemValue, String errorMessage) {
        super();
        this.validationErrors = Collections.singletonList(new ValidationError(
                problemField, problemValue, errorMessage
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

    @Getter
    public static class ValidationError {
        private final String field;
        private final String rejectedValue;
        private final String message;

        /**
         * Creates an individual Validation Error to be thrown as part of Validation Exception
         *
         * @param problemField name of the request field that failed validation
         * @param problemValue value passed in request
         * @param errorMessage error message to be provided
         */
        public ValidationError(String problemField, String problemValue, String errorMessage) {
            this.field = problemField;
            this.rejectedValue = problemValue;
            this.message = errorMessage;
        }
    }

}