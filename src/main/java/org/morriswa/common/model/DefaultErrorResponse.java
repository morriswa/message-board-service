package org.morriswa.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.GregorianCalendar;

/**
 * Default Response to send when an Exception needs to be returned
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class DefaultErrorResponse {
    private String error;
    private String message;
    private GregorianCalendar timestamp;
    private Object stack;
}
