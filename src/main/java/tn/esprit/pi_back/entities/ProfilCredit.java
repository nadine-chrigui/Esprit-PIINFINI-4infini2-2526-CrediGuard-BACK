package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ProfilDefaultFlag;
import tn.esprit.pi_back.entities.enums.ProfilHomeOwnership;
import tn.esprit.pi_back.entities.enums.ProfilLoanGrade;
import tn.esprit.pi_back.entities.enums.ProfilLoanIntent;

import java.time.LocalDateTime;

@Entity
@Table(name = "profil_credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "client")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfilCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "personAge is required")
    @Min(value = 18, message = "personAge must be >= 18")
    @Max(value = 100, message = "personAge must be <= 100")
    @Column(nullable = false)
    private Integer personAge;

    @NotNull(message = "personIncomeAnnual is required")
    @Positive(message = "personIncomeAnnual must be positive")
    @Column(nullable = false)
    private Double personIncomeAnnual;

    @NotNull(message = "personHomeOwnership is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfilHomeOwnership personHomeOwnership;

    @NotNull(message = "personEmploymentLength is required")
    @PositiveOrZero(message = "personEmploymentLength must be >= 0")
    @Column(nullable = false)
    private Double personEmploymentLength;

    @NotNull(message = "previousDefaultOnFile is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfilDefaultFlag previousDefaultOnFile;

    @NotNull(message = "creditHistoryLength is required")
    @Min(value = 1, message = "creditHistoryLength must be >= 1")
    @Column(nullable = false)
    private Integer creditHistoryLength;

    @NotNull(message = "loanIntent is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfilLoanIntent loanIntent;

    // Champs admin uniquement
    @Enumerated(EnumType.STRING)
    private ProfilLoanGrade proposedLoanGrade;

    @Positive(message = "proposedInterestRate must be positive")
    private Double proposedInterestRate;

    @NotNull(message = "monthlyFixedCharges is required")
    @PositiveOrZero(message = "monthlyFixedCharges must be >= 0")
    @Column(nullable = false)
    private Double monthlyFixedCharges;

    @NotNull(message = "existingLoanMonthlyPayments is required")
    @PositiveOrZero(message = "existingLoanMonthlyPayments must be >= 0")
    @Column(nullable = false)
    private Double existingLoanMonthlyPayments;

    @NotNull(message = "outstandingOldDebt is required")
    @PositiveOrZero(message = "outstandingOldDebt must be >= 0")
    @Column(nullable = false)
    private Double outstandingOldDebt;

    @NotNull(message = "projectStartDelayMonths is required")
    @Min(value = 0, message = "projectStartDelayMonths must be >= 0")
    @Max(value = 12, message = "projectStartDelayMonths must be <= 12")
    @Column(nullable = false)
    private Integer projectStartDelayMonths;

    @NotNull(message = "expectedMonthlyRevenueAfterStart is required")
    @PositiveOrZero(message = "expectedMonthlyRevenueAfterStart must be >= 0")
    @Column(nullable = false)
    private Double expectedMonthlyRevenueAfterStart;

    @NotNull(message = "hasExistingClients is required")
    @Column(nullable = false)
    private Boolean hasExistingClients;

    @NotNull(message = "needsGracePeriod is required")
    @Column(nullable = false)
    private Boolean needsGracePeriod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private User client;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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
