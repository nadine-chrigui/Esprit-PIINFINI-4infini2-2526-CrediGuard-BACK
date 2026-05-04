package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"investor", "project", "returnPayments"})
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long investmentId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be > 0")
    @Column(nullable = false)
    private Double amount;

    @NotNull(message = "investmentDate is required")
    @Column(nullable = false)
    private LocalDate investmentDate;

    @NotNull(message = "expectedReturn is required")
    @PositiveOrZero(message = "expectedReturn must be >= 0")
    @Column(nullable = false)
    private Double expectedReturn;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    @NotNull(message = "investor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_user_id", nullable = false)
    private User investor;

    @NotNull(message = "project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CrowdfundingProject project;

    // ReturnPayments linked directly to Investment (Transaction removed)
    @OneToMany(mappedBy = "investment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnPayment> returnPayments;

    public enum InvestmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}
