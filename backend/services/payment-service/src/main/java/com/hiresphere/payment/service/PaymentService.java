package com.hiresphere.payment.service;

import com.hiresphere.common.exception.BadRequestException;
import com.hiresphere.common.exception.ResourceNotFoundException;
import com.hiresphere.payment.client.CompanyClient;
import com.hiresphere.payment.config.RazorpayProperties;
import com.hiresphere.payment.domain.OrderStatus;
import com.hiresphere.payment.domain.Plan;
import com.hiresphere.payment.domain.PaymentOrder;
import com.hiresphere.payment.domain.Subscription;
import com.hiresphere.payment.domain.SubscriptionStatus;
import com.hiresphere.payment.dto.PaymentDtos.CreateOrderRequest;
import com.hiresphere.payment.dto.PaymentDtos.CreditCheck;
import com.hiresphere.payment.dto.PaymentDtos.OrderResponse;
import com.hiresphere.payment.dto.PaymentDtos.PlanResponse;
import com.hiresphere.payment.dto.PaymentDtos.SubscriptionResponse;
import com.hiresphere.payment.dto.PaymentDtos.VerifyRequest;
import com.hiresphere.payment.repository.PaymentOrderRepository;
import com.hiresphere.payment.repository.PlanRepository;
import com.hiresphere.payment.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String FREE = "FREE";

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentOrderRepository orderRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final CompanyClient companyClient;
    private final PaymentEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans() {
        return planRepository.findAllByOrderByPriceInrAsc().stream().map(this::toPlan).toList();
    }

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        if (!razorpayClient.isConfigured()) {
            throw new PaymentsNotConfiguredException();
        }
        Long companyId = companyClient.companyIdForOwner(userId);
        Plan plan = plan(request.planCode());
        if (plan.getPriceInr().signum() <= 0) {
            throw new BadRequestException("The " + plan.getCode() + " plan is free and needs no payment");
        }
        String receipt = "rcpt_" + companyId + "_" + System.currentTimeMillis();
        String razorpayOrderId = razorpayClient.createOrder(plan.getPriceInr(),
                razorpayProperties.currency(), receipt);

        PaymentOrder order = new PaymentOrder();
        order.setCompanyId(companyId);
        order.setPlanCode(plan.getCode());
        order.setRazorpayOrderId(razorpayOrderId);
        order.setAmount(plan.getPriceInr());
        order.setCurrency(razorpayProperties.currency());
        order.setStatus(OrderStatus.CREATED);
        orderRepository.save(order);

        return new OrderResponse(razorpayOrderId, plan.getPriceInr(), razorpayProperties.currency(),
                razorpayProperties.keyId(), plan.getCode());
    }

    @Transactional
    public SubscriptionResponse verify(Long userId, String ownerEmail, VerifyRequest request) {
        if (!razorpayClient.isConfigured()) {
            throw new PaymentsNotConfiguredException();
        }
        PaymentOrder order = orderRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
        boolean valid = RazorpaySignature.verifyPayment(request.razorpayOrderId(),
                request.razorpayPaymentId(), request.razorpaySignature(), razorpayProperties.keySecret());
        if (!valid) {
            order.setStatus(OrderStatus.FAILED);
            throw new BadRequestException("Payment signature verification failed");
        }
        order.setStatus(OrderStatus.PAID);
        order.setRazorpayPaymentId(request.razorpayPaymentId());

        Plan plan = plan(order.getPlanCode());
        Subscription subscription = activate(order.getCompanyId(), plan);
        eventPublisher.publishCompleted(order, ownerEmail);
        return toSubscription(subscription);
    }

    @Transactional
    public void handleWebhook(String rawBody, String signature) {
        if (!RazorpaySignature.verifyWebhook(rawBody, signature, razorpayProperties.webhookSecret())) {
            throw new BadRequestException("Invalid webhook signature");
        }
        // Signature valid; verification endpoint already activates subscriptions (idempotent here).
    }

    @Transactional
    public SubscriptionResponse mySubscription(Long userId) {
        Long companyId = companyClient.companyIdForOwner(userId);
        return toSubscription(ensureSubscription(companyId));
    }

    @Transactional
    public CreditCheck consumeJobCredit(Long companyId) {
        Subscription subscription = ensureSubscription(companyId);
        if (subscription.getCreditsLeft() <= 0) {
            return new CreditCheck(false, 0);
        }
        subscription.setCreditsLeft(subscription.getCreditsLeft() - 1);
        return new CreditCheck(true, subscription.getCreditsLeft());
    }

    // ---- helpers ----

    private Subscription activate(Long companyId, Plan plan) {
        Subscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseGet(() -> {
                    Subscription s = new Subscription();
                    s.setCompanyId(companyId);
                    return s;
                });
        subscription.setPlanCode(plan.getCode());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS));
        subscription.setCreditsLeft(subscription.getCreditsLeft() + plan.getJobCredits());
        return subscriptionRepository.save(subscription);
    }

    private Subscription ensureSubscription(Long companyId) {
        return subscriptionRepository.findByCompanyId(companyId).orElseGet(() -> {
            Plan free = plan(FREE);
            Subscription s = new Subscription();
            s.setCompanyId(companyId);
            s.setPlanCode(FREE);
            s.setStatus(SubscriptionStatus.ACTIVE);
            s.setCreditsLeft(free.getJobCredits());
            return subscriptionRepository.save(s);
        });
    }

    private Plan plan(String code) {
        return planRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Unknown plan: " + code));
    }

    private PlanResponse toPlan(Plan p) {
        return new PlanResponse(p.getCode(), p.getName(), p.getPriceInr(), p.getJobCredits(),
                p.getFeaturedListings(), p.getDescription());
    }

    private SubscriptionResponse toSubscription(Subscription s) {
        return new SubscriptionResponse(s.getCompanyId(), s.getPlanCode(), s.getStatus(),
                s.getValidUntil(), s.getCreditsLeft());
    }
}
