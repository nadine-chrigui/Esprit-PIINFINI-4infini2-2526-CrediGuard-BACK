package tn.esprit.pi_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemboursementResponse {

    private Long idRemboursement;
    private Double montant;
    private LocalDateTime dateRemboursement;
    private String mode;
    private Long creditId;
    private Long transactionId;
}
