package com.hiresphere.payment.web;

import com.hiresphere.common.dto.ApiResponse;
import com.hiresphere.common.exception.ForbiddenException;
import com.hiresphere.common.security.UserContext;
import com.hiresphere.common.security.UserContextHolder;
import com.hiresphere.payment.dto.PaymentDtos.CreateOrderRequest;
import com.hiresphere.payment.dto.PaymentDtos.OrderResponse;
import com.hiresphere.payment.dto.PaymentDtos.PlanResponse;
import com.hiresphere.payment.dto.PaymentDtos.SubscriptionResponse;
import com.hiresphere.payment.dto.PaymentDtos.VerifyRequest;
import com.hiresphere.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/api/plans")
    public ApiResponse<List<PlanResponse>> plans() {
        return ApiResponse.ok(paymentService.listPlans());
    }

    @PostMapping("/api/payments/orders")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(paymentService.createOrder(requireEmployer().userId(), request));
    }

    @PostMapping("/api/payments/verify")
    public ApiResponse<SubscriptionResponse> verify(@Valid @RequestBody VerifyRequest request) {
        UserContext ctx = requireEmployer();
        return ApiResponse.ok("Payment verified", paymentService.verify(ctx.userId(), ctx.email(), request));
    }

    @PostMapping("/api/payments/webhook")
    public ApiResponse<Void> webhook(@RequestBody String rawBody,
                                     @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(rawBody, signature);
        return ApiResponse.message("ok");
    }

    @GetMapping("/api/subscriptions/my")
    public ApiResponse<SubscriptionResponse> mySubscription() {
        return ApiResponse.ok(paymentService.mySubscription(requireEmployer().userId()));
    }

    private UserContext requireEmployer() {
        UserContext ctx = UserContextHolder.require();
        if (!ctx.isEmployer()) {
            throw new ForbiddenException("Only employers can manage billing");
        }
        return ctx;
    }
}
