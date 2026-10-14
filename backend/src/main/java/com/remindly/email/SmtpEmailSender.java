package com.remindly.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender implements EmailSender {
    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;

    public SmtpEmailSender(JavaMailSender mailSender,
                           @Value("${email.from}") String from,
                           @Value("${email.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
    }

    @Override
    public void send(String recipient, String subject, String plainText, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException exception) {
            throw new EmailDeliveryException(exception);
        }
    }
}
