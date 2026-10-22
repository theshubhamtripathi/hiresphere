package com.hiresphere.payment.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpaySignatureTest {

    private static final String SECRET = "test_secret_key";

    @Test
    void verifiesValidPaymentSignature() {
        String expected = RazorpaySignature.hmacHex("order_123|pay_456", SECRET);
        assertThat(RazorpaySignature.verifyPayment("order_123", "pay_456", expected, SECRET)).isTrue();
    }

    @Test
    void rejectsTamperedPaymentSignature() {
        String expected = RazorpaySignature.hmacHex("order_123|pay_456", SECRET);
        assertThat(RazorpaySignature.verifyPayment("order_123", "pay_999", expected, SECRET)).isFalse();
        assertThat(RazorpaySignature.verifyPayment("order_123", "pay_456", "deadbeef", SECRET)).isFalse();
    }

    @Test
    void rejectsWhenSecretMissing() {
        assertThat(RazorpaySignature.verifyPayment("o", "p", "sig", "")).isFalse();
        assertThat(RazorpaySignature.verifyPayment("o", "p", "sig", null)).isFalse();
    }

    @Test
    void verifiesWebhookSignature() {
        String body = "{\"event\":\"payment.captured\"}";
        String expected = RazorpaySignature.hmacHex(body, SECRET);
        assertThat(RazorpaySignature.verifyWebhook(body, expected, SECRET)).isTrue();
        assertThat(RazorpaySignature.verifyWebhook(body, "bad", SECRET)).isFalse();
    }
}
