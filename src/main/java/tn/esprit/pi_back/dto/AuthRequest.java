package tn.esprit.pi_back.dto;

import lombok.Data;
@Data
public class AuthRequest {
    private String email;
        private String password;
    }
