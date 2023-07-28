package org.morriswa.common.model;

import lombok.*;
import org.springframework.http.ResponseEntity;

import java.util.GregorianCalendar;

/**
 * Default Response Wrapper
 * @param <T> the Type of data being returned
 */
@Getter
public class DefaultResponse<T> {
    private final String message;
    private final GregorianCalendar timestamp;
    private final T payload;

    public DefaultResponse(String message) {
        this.message = message;
        this.timestamp = new GregorianCalendar();
        this.payload = null;
    };

    public DefaultResponse(String message, T payload) {
        this.message = message;
        this.timestamp = new GregorianCalendar();
        this.payload = payload;
    };
}
