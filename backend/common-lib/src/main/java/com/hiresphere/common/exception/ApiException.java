package com.hiresphere.common.exception;

import lombok.Getter;

/** Base for all domain exceptions that map to a specific HTTP status. */
@Getter
public abstract class ApiException extends RuntimeException {

    private final int status;
    private final String error;

    protected ApiException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }
}
