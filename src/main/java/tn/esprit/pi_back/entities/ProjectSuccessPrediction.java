package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "project")
public class ProjectSuccessPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CrowdfundingProject project;

    @Column(nullable = false)
    private Double successProbability;

    @Column(nullable = false, length = 20)
    private String predictedLabel;

    @Column(nullable = false, length = 100)
    private String modelVersion;

    @Column(nullable = false)
    private Double trainingAccuracy;

    @Column(nullable = false)
    private Double validationAccuracy;

    @Column(nullable = false)
    private Integer daysSinceLaunch;

    @Column(nullable = false)
    private Integer campaignDurationDays;

    @Column(nullable = false)
    private Double fundingProgressRatio;

    @Column(nullable = false)
    private Double collectedAmount;

    @Column(nullable = false)
    private Double fundingGoal;

    @Column(nullable = false)
    private Double averageInvestmentAmount;

    @Column(nullable = false)
    private Integer investmentCount;

    @Column(nullable = false)
    private Integer purchaseOptionSubscriptionsCount;

    @Column(nullable = false)
    private Integer blogPostsLast7Days;

    @Column(nullable = false)
    private Double premiumInvestorRatio;

    @Column(nullable = false)
    private Double stableInvestorRatio;

    @Column(nullable = false)
    private Double riskyInvestorRatio;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
