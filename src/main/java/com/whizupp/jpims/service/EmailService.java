package com.whizupp.jpims.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
