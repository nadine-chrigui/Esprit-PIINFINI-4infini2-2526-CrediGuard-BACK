package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.services.SmsVerificationService;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SmsVerificationController {

    private final SmsVerificationService smsService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        smsService.sendOtp(phone);
        return ResponseEntity.ok(Map.of("status", "SMS_SENT"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        String code = request.get("code");
        boolean isValid = smsService.verifyOtp(phone, code);
        return ResponseEntity.ok(isValid);
    }
}
