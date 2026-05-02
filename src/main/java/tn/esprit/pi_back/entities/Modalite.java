package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DecisionModalite;
import tn.esprit.pi_back.entities.enums.TypeModalite;

import java.time.LocalDateTime;

@Entity
@Table(name = "modalite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"demandeCredit", "evaluationRisque"})
public class Modalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeModalite modaliteRecommandee;

    @Enumerated(EnumType.STRING)
    private TypeModalite modaliteChoisie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionModalite decision;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Column(nullable = false)
    private Double tauxInteretAnnuel;

    private Double revenuMensuelActuel;
    private Double revenuFuturReconnu;
    private Double revenuTotalReconnu;
    private Double chargesMensuellesTotales;
    private Double capaciteMensuelleMax;

    private Double mensualiteAmortissable;
    private Double mensualiteInFine;
    private Double mensualiteGrace;

    private Boolean graceActive;
    private Integer dureeGraceMois;
    private Integer dureeEffectiveMois;

    private Double probabiliteDefaut;
    private Double var95;
    private Double cvar95;
    private Double scoreCredit;
    private String niveauRisque;

    private Double dti;
    private Double paymentToIncome;
    private Double lti;
    private Boolean financialStress;

    private Double coutTotalAmortissable;
    private Double coutTotalInFine;

    private String commentaireAdmin;
    private String choisiePar;
    private LocalDateTime dateChoix;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", nullable = false, unique = true)
    private DemandeCredit demandeCredit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_risque_id")
    private EvaluationRisque evaluationRisque;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
