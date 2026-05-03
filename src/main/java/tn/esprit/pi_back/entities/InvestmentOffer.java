package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.InvestmentType;
import tn.esprit.pi_back.entities.enums.OfferStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "investment_offer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InvestmentOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private InvestmentType type;

    private String riskLevel; // e.g., "Faible", "Modéré", "Elevé"

    private double estimatedReturn; // percentage

    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "investmentOffer", cascade = CascadeType.ALL)
    private List<PerformanceTracking> performanceHistory;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OfferStatus.PROPOSED;
    }
}