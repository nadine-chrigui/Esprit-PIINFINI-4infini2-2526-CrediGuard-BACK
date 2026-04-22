package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import tn.esprit.pi_back.entities.enums.UserType;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;

    @Pattern(
            regexp = "^(\\+216)?[24579][0-9]{7}$",
            message = "phone must be a valid Tunisian number: 20123456 or +21620123456"
    )
    private String phone;

    @NotNull(message = "userType is required")
    private UserType userType;

    @NotNull(message = "enabled is required")
    private Boolean enabled;

    // optionnel en modification
    private String password;
}