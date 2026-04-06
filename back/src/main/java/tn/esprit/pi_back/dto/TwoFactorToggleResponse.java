package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwoFactorToggleResponse {
    private String message;
    private Boolean twoFactorEnabled;
}