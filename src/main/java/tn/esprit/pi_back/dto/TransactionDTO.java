package tn.esprit.pi_back.dto;

import lombok.Data;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long idTransaction;
    private TransactionType typeTransaction;
    private Double montant;
    private LocalDateTime dateTransaction;
    private TransactionStatut statut;
    private Long compteSourceId;
    private Long compteDestinationId;
    private Long orderId;
}