package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private boolean requiresTwoFactor;
    private String accessToken;
    private String message;
    private String email;
    private String userType;
}