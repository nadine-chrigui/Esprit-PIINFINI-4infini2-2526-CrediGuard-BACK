package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alertType; // MONTANT_CRITIQUE, FREQUENCE_ELEVEE, COMPORTEMENT_INHABITUEL

    private Integer riskScore; // 0-100

    private String status; // SURVEILLANCE, OUVERTE, RESOLUE

    private String actionTaken; // NOTIFIE, CARTE_BLOQUEE

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_transaction")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "id_carte")
    private Carte carte;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
