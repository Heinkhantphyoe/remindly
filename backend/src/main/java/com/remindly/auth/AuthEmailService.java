package com.remindly.auth;

import com.remindly.email.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AuthEmailService {
    private final EmailSender emailSender;
    private final String appUrl;

    public AuthEmailService(EmailSender emailSender, @Value("${email.app-url}") String appUrl) {
        this.emailSender = emailSender; this.appUrl = appUrl.replaceAll("/$", "");
    }

    public void sendVerification(String recipient, String token) {
        String link = appUrl + "/verify-email?token=" + encode(token);
        emailSender.send(recipient, "Verify your Remindly email", "Verify your email: " + link,
                "<p>Welcome to Remindly.</p><p><a href=\"" + link + "\">Verify your email address</a></p><p>This link expires in 24 hours.</p>");
    }

    public void sendPasswordReset(String recipient, String token) {
        String link = appUrl + "/reset-password?token=" + encode(token);
        emailSender.send(recipient, "Reset your Remindly password", "Reset your password: " + link,
                "<p>Someone requested a password reset for your Remindly account.</p><p><a href=\"" + link + "\">Choose a new password</a></p><p>This link expires in 1 hour.</p>");
    }

    private String encode(String token) { return URLEncoder.encode(token, StandardCharsets.UTF_8); }
}
