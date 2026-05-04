package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;
}