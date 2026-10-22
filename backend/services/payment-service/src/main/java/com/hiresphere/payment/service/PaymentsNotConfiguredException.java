package com.hiresphere.payment.service;

import com.hiresphere.common.exception.ApiException;

public class PaymentsNotConfiguredException extends ApiException {

    public PaymentsNotConfiguredException() {
        super(503, "Service Unavailable", "Payments are not configured on this instance");
    }
}
