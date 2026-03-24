package tn.esprit.pi_back.services;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}