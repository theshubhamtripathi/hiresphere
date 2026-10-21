package com.hiresphere.common.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(403, "Forbidden", message);
    }
}
