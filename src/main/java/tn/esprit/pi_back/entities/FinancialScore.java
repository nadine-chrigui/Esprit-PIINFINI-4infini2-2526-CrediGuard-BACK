package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class FinancialScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer score; // 0-1000

    private String level; // FAIBLE, MOYEN, BON, EXCELLENT

    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "id_utilisateur")
    private User user;

    // Facteurs détaillés (JSON or separate columns)
    private Double regularityFactor;
    private Double reimbursementRatioFactor;
    private Double spendingDiversityFactor;
    private Double fraudAbsenceFactor;
    private Double savingsFactor;

    @PrePersist @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
