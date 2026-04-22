package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ModeRemboursement;
import tn.esprit.pi_back.entities.enums.StatutCredit;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"client", "demandeCredit", "remboursements"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "montantAccorde is required")
    @Positive(message = "montantAccorde must be positive")
    @Column(nullable = false)
    private Double montantAccorde;

    @NotNull(message = "montantTotal is required")
    @Positive(message = "montantTotal must be positive")
    @Column(nullable = false)
    private Double montantTotal;

    @NotNull(message = "montantRestant is required")
    @PositiveOrZero(message = "montantRestant must be >= 0")
    @Column(nullable = false)
    private Double montantRestant;

    @NotNull(message = "tauxRemboursement is required")
    @PositiveOrZero(message = "tauxRemboursement must be >= 0")
    @Column(nullable = false)
    private Double tauxRemboursement;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @NotNull(message = "statut is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCredit statut;

    @NotNull(message = "modeRemboursement is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModeRemboursement modeRemboursement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", nullable = false, unique = true)
    private DemandeCredit demandeCredit;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Remboursement> remboursements;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateDebut == null) {
            dateDebut = LocalDateTime.now();
        }
        if (montantTotal == null && montantAccorde != null) {
            montantTotal = montantAccorde;
        }
        if (montantRestant == null && montantTotal != null) {
            montantRestant = montantTotal;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
