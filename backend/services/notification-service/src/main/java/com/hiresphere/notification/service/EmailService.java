package com.hiresphere.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/** Renders a Thymeleaf HTML template and sends it via SMTP (Mailpit in dev). */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${hiresphere.mail.from:no-reply@hiresphere.local}")
    private String from;

    public void send(String to, String subject, String template, Map<String, Object> variables) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            Context context = new Context();
            context.setVariables(variables);
            String html = templateEngine.process("email/" + template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.debug("Sent '{}' email to {}", subject, to);
        } catch (Exception ex) {
            log.warn("Failed to send '{}' email to {}: {}", subject, to, ex.getMessage());
        }
    }
}
