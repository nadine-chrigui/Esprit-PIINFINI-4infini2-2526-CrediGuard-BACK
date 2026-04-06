package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemboursementRequest {

    @NotNull(message = "montant is required")
    @Positive(message = "montant must be > 0")
    private Double montant;

    private String mode;

    @NotNull(message = "creditId is required")
    private Long creditId;

    private Long transactionId;
}
