package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.UserType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {
    private Long id;
    private String email;
    private UserType userType;
    private String role;
}