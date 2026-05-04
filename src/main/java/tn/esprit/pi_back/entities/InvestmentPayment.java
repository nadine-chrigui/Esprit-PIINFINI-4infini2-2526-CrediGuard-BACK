package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import tn.esprit.pi_back.entities.enums.InvestmentPaymentStatus;
import tn.esprit.pi_back.entities.enums.PaymentScheduleFrequency;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"investor", "project", "investment"})
public class InvestmentPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private User investor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CrowdfundingProject project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id", unique = true)
    private Investment investment;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double amount;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String currency;

    @NotNull
    @Column(nullable = false)
    private Integer durationYears;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentScheduleFrequency scheduleFrequency;

    @NotNull
    @Column(nullable = false)
    private Double interestRateSnapshot;

    @NotNull
    @Column(nullable = false)
    private Double expectedReturnSnapshot;

    @Column(unique = true)
    private String stripePaymentIntentId;

    @Column(length = 255)
    private String stripeClientSecret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentPaymentStatus status = InvestmentPaymentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InvestmentPaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
