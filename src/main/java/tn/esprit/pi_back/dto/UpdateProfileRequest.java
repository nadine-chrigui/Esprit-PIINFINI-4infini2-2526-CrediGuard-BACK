package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;

    @Pattern(
            regexp = "^(\\+216)?\\d+$|^$",
            message = "phone must be a valid number"
    )
    private String phone;

    private String sector;
    private String activityType;
    private String region;
}