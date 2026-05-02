package tn.esprit.pi_back.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@Slf4j
public class SmsVerificationService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromNumber;

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        String code;
        LocalDateTime expiry;

        OtpData(String code) {
            this.code = code;
            this.expiry = LocalDateTime.now().plusMinutes(5);
        }
    }

    @PostConstruct
    public void initTwilio() {
        if (accountSid != null && !accountSid.startsWith("ACxxx")) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully.");
        } else {
            log.warn("Twilio credentials not provided. SMS will be simulated in logs.");
        }
    }

    public String sendOtp(String phoneNumber) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        otpStorage.put(phoneNumber, new OtpData(code));
        
        String messageBody = "Votre code de vérification CrediGuard est : " + code;

        if (accountSid != null && !accountSid.startsWith("ACxxx")) {
            try {
                Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(fromNumber),
                        messageBody
                ).create();
                log.info("Real SMS sent to {}", phoneNumber);
            } catch (Exception e) {
                log.error("Failed to send real SMS: {}", e.getMessage());
            }
        }
        
        // Simulation d'envoi via Trellio (Twilio)
        log.info("===> [SIMULATION SMS] SENT TO {}: {}", phoneNumber, messageBody);
        log.info("===> VOUS POUVEZ COPIER CE CODE POUR TESTER : {}", code);
        
        return "OTP_SENT";
    }

    public boolean verifyOtp(String phoneNumber, String code) {
        OtpData data = otpStorage.get(phoneNumber);
        if (data == null) return false;
        
        if (data.expiry.isBefore(LocalDateTime.now())) {
            otpStorage.remove(phoneNumber);
            return false;
        }
        
        boolean isValid = data.code.equals(code);
        if (isValid) {
            otpStorage.remove(phoneNumber);
        }
        return isValid;
    }
}
