package org.morriswa.messageboard.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ResourceException extends Exception {
    public ResourceException(String msg) {
        super(msg);
    }
}
