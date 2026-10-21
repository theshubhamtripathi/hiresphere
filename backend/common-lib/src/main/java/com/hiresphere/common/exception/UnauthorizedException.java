package com.hiresphere.common.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(401, "Unauthorized", message);
    }
}
