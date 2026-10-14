package com.remindly.email;

public interface EmailSender {
    void send(String recipient, String subject, String plainText, String html);
}
