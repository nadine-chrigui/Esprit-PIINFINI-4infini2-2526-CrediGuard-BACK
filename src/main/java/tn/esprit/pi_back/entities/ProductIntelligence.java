package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ProductPerformanceLabel;
import tn.esprit.pi_back.entities.enums.ProductRiskLevel;
import tn.esprit.pi_back.entities.enums.ProductSuggestedAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "product")
public class ProductIntelligence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductRiskLevel riskLevel;

    @Column(nullable = false)
    private Integer daysToStockout;

    @Column(nullable = false)
    private Integer recommendedRestock;

    @Column(nullable = false)
    private Integer performanceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductPerformanceLabel performanceLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSuggestedAction suggestedAction;

    @Column(length = 1200)
    private String reasons;

    private Integer riskConfidence;

    private Integer actionConfidence;

    @Column(length = 700)
    private String mlDecision;

    @Column(length = 700)
    private String mainDrivers;

    @Column(length = 700)
    private String businessRecommendation;

    @Column(length = 180)
    private String modelType;

    @Column(nullable = false)
    private Integer salesLast7Days;

    @Column(nullable = false)
    private Integer salesLast30Days;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        analyzedAt = LocalDateTime.now();
    }
}
