package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.esprit.pi_back.entities.enums.PartnerStatus;
import tn.esprit.pi_back.entities.enums.PartnerType;
import tn.esprit.pi_back.entities.enums.UserType;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;

    private String phone;

    @NotNull(message = "userType is required")
    private UserType userType;

    @NotNull(message = "enabled is required")
    private Boolean enabled;

    // optionnel en modification
    private String password;
    private PartnerType partnerType;
    private PartnerStatus partnerStatus;
}