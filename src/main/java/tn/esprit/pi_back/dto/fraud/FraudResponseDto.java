package tn.esprit.pi_back.dto.fraud;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudResponseDto {
    private String transactionId;
    private Double scoreFraude;
    private Double probabiliteRF;
    private String decision;
    private String niveau;
    private List<String> raisons;
    private String timestamp;
}