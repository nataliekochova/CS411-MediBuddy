package com.medibuddy.service;

import java.io.ObjectInputFilter.Config;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

public class EmailService {

    private final Resend resend;

    public EmailService() {
        String apiKey = AppConfig.getResendApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing RESEND_API_KEY in config.properties.");
        }

        this.resend = new Resend(apiKey);
    }

    public void sendTestEmail(String toEmail) {
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from("MediBuddy <onboarding@resend.dev>")
                .to(toEmail)
                .subject("MediBuddy Emergency Contact Test")
                .html("<p>This is a test emergency contact email from <strong>MediBuddy</strong>.</p>")
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(request);
            System.out.println("Email sent. ID: " + response.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send test email: " + e.getMessage(), e);
        }
    }

    public void sendCriticalAlert(String toEmail, String medicationName, String scheduledTime) {
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from("MediBuddy <onboarding@resend.dev>")
                .to(toEmail)
                .subject("MediBuddy Critical Alert")
                .html("""
                        <p><strong>Critical Alert:</strong></p>
                        <p>A medication may have been missed.</p>
                        <p><strong>Medication:</strong> %s</p>
                        <p><strong>Scheduled time:</strong> %s</p>
                        """.formatted(medicationName, scheduledTime))
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(request);
            System.out.println("Critical alert email sent. ID: " + response.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send critical alert email: " + e.getMessage(), e);
        }
    }
}