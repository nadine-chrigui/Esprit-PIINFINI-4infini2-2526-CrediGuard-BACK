package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    //private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Email simulé : " + subject);
    }
}