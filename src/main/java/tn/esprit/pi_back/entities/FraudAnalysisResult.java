package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString
public class FraudAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;
    private Long carteId;
    private Long userId;

    private Double scoreFraude;
    private Double probabiliteRF;
    
    private String decision; // BLOQUEE, ALERTE, ACCEPTEE
    private String niveau;   // CRITIQUE, MOYEN, FAIBLE

    @ElementCollection
    @CollectionTable(name = "fraud_reasons", joinColumns = @JoinColumn(name = "fraud_id"))
    @Column(name = "reason")
    private List<String> raisons;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
