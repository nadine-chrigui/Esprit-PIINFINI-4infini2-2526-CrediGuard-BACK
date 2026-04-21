package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean requiresTwoFactor;
    private String accessToken;
    private String message;
    private Long id;
    private String email;
    private String userType;
    private AuthUserDto user;
}