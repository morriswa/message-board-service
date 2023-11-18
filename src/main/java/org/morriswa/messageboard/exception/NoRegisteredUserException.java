package org.morriswa.messageboard.exception;

public class NoRegisteredUserException extends Exception {
    public NoRegisteredUserException(String msg) {
        super(msg);
    }
}
