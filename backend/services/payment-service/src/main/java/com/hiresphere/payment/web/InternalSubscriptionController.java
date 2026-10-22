package com.hiresphere.payment.web;

import com.hiresphere.payment.dto.PaymentDtos.CreditCheck;
import com.hiresphere.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Job-credit checks for job-service. Never exposed through the gateway. */
@RestController
@RequestMapping("/api/internal/subscriptions")
@RequiredArgsConstructor
public class InternalSubscriptionController {

    private final PaymentService paymentService;

    @PostMapping("/company/{companyId}/consume-job-credit")
    public CreditCheck consumeJobCredit(@PathVariable Long companyId) {
        return paymentService.consumeJobCredit(companyId);
    }
}
