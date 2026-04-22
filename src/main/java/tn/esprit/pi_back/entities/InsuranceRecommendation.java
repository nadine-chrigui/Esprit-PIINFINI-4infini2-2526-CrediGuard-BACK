package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InsuranceRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_credit_id", nullable = false)
    private DemandeCredit demandeCredit;

    private Double riskScore;

    @Column(length = 2000)
    private String recommendationText;

    @ManyToMany
    @JoinTable(
        name = "recommendation_offers",
        joinColumns = @JoinColumn(name = "recommendation_id"),
        inverseJoinColumns = @JoinColumn(name = "offer_id")
    )
    private List<InsuranceOffer> suggestedOffers;

    private LocalDateTime calculationDate;

    @PrePersist
    void onCreate() {
        calculationDate = LocalDateTime.now();
    }
}
