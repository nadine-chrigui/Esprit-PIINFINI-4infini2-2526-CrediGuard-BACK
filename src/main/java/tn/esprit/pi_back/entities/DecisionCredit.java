package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DecisionFinale;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"demandeCredit"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DecisionCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "decisionFinale is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionFinale decisionFinale;

    @NotBlank(message = "justification is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(nullable = false)
    private LocalDateTime dateDecision;

    @NotBlank(message = "prisePar is required")
    @Column(nullable = false)
    private String prisePar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", nullable = false)
    private DemandeCredit demandeCredit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateDecision == null) {
            dateDecision = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
