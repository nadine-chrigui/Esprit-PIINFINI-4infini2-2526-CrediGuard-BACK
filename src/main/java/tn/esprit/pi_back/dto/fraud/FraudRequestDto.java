package tn.esprit.pi_back.dto.fraud;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRequestDto {
    private String transactionId;
    private Double montant;
    private Integer heure;
    private Integer nbTransactions24h;
    private Integer nbTransactions7j;
    private Double montantTotal24h;
    private Integer joursdepuisCreation;
    private Integer estNouveauMarchand;
    private Integer estWeekend;
    private Integer paysDifferent;
    private String categorie;
    private Double scoreHistorique;
}