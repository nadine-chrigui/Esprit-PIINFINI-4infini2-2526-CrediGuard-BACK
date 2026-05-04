package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.CompteType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteFinancierRequest {

    @NotNull(message = "solde is required")
    @PositiveOrZero(message = "solde must be >= 0")
    private Double solde;

    @NotNull(message = "typeCompte is required")
    private CompteType typeCompte;

    private Long utilisateurId;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String idType;
}