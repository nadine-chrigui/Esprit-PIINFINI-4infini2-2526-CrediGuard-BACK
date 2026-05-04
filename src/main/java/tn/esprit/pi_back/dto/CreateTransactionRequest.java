package tn.esprit.pi_back.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import tn.esprit.pi_back.entities.enums.TransactionType;

@Data
public class CreateTransactionRequest {

    @NotNull
    private TransactionType typeTransaction;

    @NotNull
    @Positive
    private Double montant;

    @NotNull
    private Long compteSourceId;

    @NotNull
    private Long compteDestinationId;

    @NotNull
    private Long orderId;
}