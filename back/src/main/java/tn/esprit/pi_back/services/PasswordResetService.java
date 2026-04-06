package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.ForgotPasswordResponse;

public interface PasswordResetService {
    ForgotPasswordResponse forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}