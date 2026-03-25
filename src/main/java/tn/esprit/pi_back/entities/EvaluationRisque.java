package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.NiveauRisque;
import tn.esprit.pi_back.entities.enums.DecisionSuggeree;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"demandeCredit"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EvaluationRisque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "score is required")
    @Column(nullable = false)
    private Double score;

    @NotNull(message = "niveauRisque is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauRisque niveauRisque;

    @NotNull(message = "probabiliteDefaut is required")
    @PositiveOrZero(message = "probabiliteDefaut must be >= 0")
    @Column(nullable = false)
    private Double probabiliteDefaut;

    @NotBlank(message = "versionModele is required")
    @Column(nullable = false)
    private String versionModele;

    @Column(nullable = false)
    private LocalDateTime dateEvaluation;

    @NotNull(message = "decisionSuggeree is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionSuggeree decisionSuggeree;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", nullable = false)
    private DemandeCredit demandeCredit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateEvaluation == null) {
            dateEvaluation = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
