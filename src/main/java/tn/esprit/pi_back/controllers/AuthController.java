package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.*;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.security.JwtService;
import tn.esprit.pi_back.services.EmailService;
import tn.esprit.pi_back.services.PasswordResetService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String otp = String.valueOf((int) (100000 + Math.random() * 900000));

            user.setOtpCode(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);

            String subject = "Your verification code";
            String body = "Hello,\n\nYour OTP code is: " + otp
                    + "\n\nThis code expires in 5 minutes.";

            emailService.sendEmail(user.getEmail(), subject, body);

            return new LoginResponse(
                    true,
                    null,
                    "OTP sent to your email",
                    user.getEmail(),
                    user.getUserType() != null ? user.getUserType().name() : null,
                    toAuthUserDto(user)
            );
        }

        String token = jwtService.generateToken(request.getEmail());

        return new LoginResponse(
                false,
                token,
                "Login successful",
                user.getEmail(),
                user.getUserType() != null ? user.getUserType().name() : null,
                toAuthUserDto(user)
        );
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return new MessageResponse("Password reset successfully");
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            throw new RuntimeException("No OTP found");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, toAuthUserDto(user));
    }

    @PostMapping("/enable-2fa")
    public TwoFactorToggleResponse enable2fa(@Valid @RequestBody TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        return new TwoFactorToggleResponse("2FA enabled successfully", true);
    }

    @PostMapping("/disable-2fa")
    public TwoFactorToggleResponse disable2fa(@Valid @RequestBody TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTwoFactorEnabled(false);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return new TwoFactorToggleResponse("2FA disabled successfully", false);
    }

    @PostMapping("/2fa-status")
    public TwoFactorToggleResponse get2faStatus(@Valid @RequestBody TwoFactorRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new TwoFactorToggleResponse(
                "2FA status fetched successfully",
                user.getTwoFactorEnabled()
        );
    }

    private AuthUserDto toAuthUserDto(User user) {
        return new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getUserType(),
                user.getUserType() != null ? user.getUserType().name() : null
        );
    }
}