package com.hiresphere.common.exception;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(400, "Bad Request", message);
    }
}
