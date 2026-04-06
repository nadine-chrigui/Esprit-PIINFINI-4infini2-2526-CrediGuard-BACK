package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long idTransaction;
    private TransactionType typeTransaction;
    private Double montant;
    private LocalDateTime dateTransaction;
    private TransactionStatut statut;
    private Long compteSourceId;
    private Long compteDestinationId;
    private Long orderId;
}
