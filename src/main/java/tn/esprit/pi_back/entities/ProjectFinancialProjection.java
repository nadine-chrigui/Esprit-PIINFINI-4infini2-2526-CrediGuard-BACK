package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"project", "purchaseOption"})
public class ProjectFinancialProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CrowdfundingProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_option_id")
    private PurchaseOption purchaseOption;

    @Column(nullable = false)
    private Double totalInvestment;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Double optionPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer soldQuantity;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Column(nullable = false)
    private Double estimatedSalesRate;

    @Column(nullable = false)
    private Double growthRate;

    @Column(nullable = false)
    private Integer durationYears;

    @Column(nullable = false)
    private Double currentRevenue;

    @Column(nullable = false)
    private Double futureRevenue;

    @Column(nullable = false)
    private Double investorCost;

    @Column(nullable = false)
    private Double netProfit;

    @Column(nullable = false, length = 1000)
    private String summaryMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
