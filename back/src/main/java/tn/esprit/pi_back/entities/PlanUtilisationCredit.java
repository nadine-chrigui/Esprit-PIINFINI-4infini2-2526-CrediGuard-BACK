package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.NatureActivite;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PlanUtilisationCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "descriptionProjet is required")
    @Column(nullable = false)
    private String descriptionProjet;

    @NotBlank(message = "objectifCredit is required")
    @Column(nullable = false)
    private String objectifCredit;

    @NotNull(message = "montantInvestissement is required")
    @Positive(message = "montantInvestissement must be positive")
    @Column(nullable = false)
    private Double montantInvestissement;

    @PositiveOrZero(message = "revenuMensuelPrevu must be >= 0")
    private Double revenuMensuelPrevu;

    @PositiveOrZero(message = "profitMensuelPrevu must be >= 0")
    private Double profitMensuelPrevu;

    @Positive(message = "delaiRentabiliteMois must be positive")
    private Integer delaiRentabiliteMois;

    @NotNull(message = "natureActivite is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NatureActivite natureActivite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", nullable = false)
    private DemandeCredit demandeCredit;
}
