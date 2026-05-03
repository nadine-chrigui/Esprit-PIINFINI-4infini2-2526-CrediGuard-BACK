package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.TransactionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "typeTransaction is required")
    private TransactionType typeTransaction;

    @NotNull(message = "montant is required")
    @Positive(message = "montant must be > 0")
    private Double montant;

    @NotNull(message = "compteSourceId is required")
    private Long compteSourceId;

    @NotNull(message = "compteDestinationId is required")
    private Long compteDestinationId;

    private Long orderId;
}
