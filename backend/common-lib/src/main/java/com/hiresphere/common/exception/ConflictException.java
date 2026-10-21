package com.hiresphere.common.exception;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(409, "Conflict", message);
    }
}
