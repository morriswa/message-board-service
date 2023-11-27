package org.morriswa.messageboard.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Validation Exception to throw when the server cannot complete a request due to user input error
 */
@Getter
public class PermissionsException extends Exception {

    private final List<String> permissionViolations;

    /**
     * Creates a Permissions Exception when only one error needs to be returned
     *
     * @param errorMessage error message to be provided
     */
    public PermissionsException(String errorMessage) {
        super();
        this.permissionViolations = Collections.singletonList(errorMessage);
    }

    /**
     * Creates a Validation Exception when multiple errors need to be returned
     *
     * @param permissionViolations to be included in response
     */
    public PermissionsException(List<String> permissionViolations) {
        super();
        this.permissionViolations = permissionViolations;
    }

}