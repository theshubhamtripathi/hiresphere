package com.hiresphere.payment.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Razorpay HMAC-SHA256 signature helpers (payment verification and webhooks). */
public final class RazorpaySignature {

    private RazorpaySignature() {
    }

    /** Payment signature is HMAC_SHA256(orderId + "|" + paymentId, keySecret). */
    public static boolean verifyPayment(String orderId, String paymentId, String signature, String keySecret) {
        return matches(orderId + "|" + paymentId, signature, keySecret);
    }

    /** Webhook signature is HMAC_SHA256(rawBody, webhookSecret). */
    public static boolean verifyWebhook(String rawBody, String signature, String webhookSecret) {
        return matches(rawBody, signature, webhookSecret);
    }

    public static String hmacHex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute HMAC", e);
        }
    }

    private static boolean matches(String data, String signature, String secret) {
        if (signature == null || secret == null || secret.isBlank()) {
            return false;
        }
        String expected = hmacHex(data, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }
}
