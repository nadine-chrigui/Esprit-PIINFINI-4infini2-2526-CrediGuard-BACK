package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.TypeCredit;
import tn.esprit.pi_back.entities.enums.StatutDemande;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"client", "voucher"})
public class DemandeCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "reference is required")
    @Column(unique = true, nullable = false)
    private String reference;

    @NotNull(message = "typeCredit is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCredit typeCredit;

    @NotNull(message = "montantDemande is required")
    @Positive(message = "montantDemande must be positive")
    @Column(nullable = false)
    private Double montantDemande;

    @NotNull(message = "dureeMois is required")
    @Positive(message = "dureeMois must be positive")
    @Column(nullable = false)
    private Integer dureeMois;

    @NotBlank(message = "objetCredit is required")
    @Column(nullable = false)
    private String objetCredit;

    @NotNull(message = "statut is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @OneToOne(mappedBy = "demandeCredit", fetch = FetchType.LAZY)
    private Voucher voucher;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
