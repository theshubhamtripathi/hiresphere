package com.hiresphere.payment.service;

import com.hiresphere.common.event.PaymentCompletedEvent;
import com.hiresphere.common.event.Topics;
import com.hiresphere.payment.domain.PaymentOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompleted(PaymentOrder order, String ownerEmail) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                order.getId(), order.getRazorpayOrderId(), order.getCompanyId(), ownerEmail,
                order.getPlanCode(), order.getAmount(), order.getCurrency(), Instant.now());
        kafkaTemplate.send(Topics.PAYMENT_COMPLETED, String.valueOf(order.getCompanyId()), event);
    }
}
