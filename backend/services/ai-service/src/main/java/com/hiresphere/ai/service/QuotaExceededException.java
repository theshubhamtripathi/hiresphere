package com.hiresphere.ai.service;

import com.hiresphere.common.exception.ApiException;

public class QuotaExceededException extends ApiException {

    public QuotaExceededException(String message) {
        super(429, "Too Many Requests", message);
    }
}
