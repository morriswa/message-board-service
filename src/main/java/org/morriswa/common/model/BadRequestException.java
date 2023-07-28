package org.morriswa.common.model;

/**
 * Generic Exception to throw when the server cannot complete a request
 */
public class BadRequestException extends Exception {
    public BadRequestException(String msg) {
        super(msg);
    }
}