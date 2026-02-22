package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"owner"})
public class CrowdfundingProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long projectId;

    @NotBlank(message = "title is required")
    @Size(min = 3, max = 50, message = "title length must be between 3 and 120")
    @Column(nullable = false, length = 50)
    private String title;

    @NotBlank(message = "description is required")
    @Size(min = 10, max = 2000, message = "description length must be between 10 and 2000")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull(message = "fundingGoal is required")
    @Positive(message = "fundingGoal must be > 0")
    @Column(nullable = false)
    private Double fundingGoal;

    @NotNull(message = "collectedAmount is required")
    @PositiveOrZero(message = "collectedAmount must be >= 0")
    @Column(nullable = false)
    private Double collectedAmount = 0.0;

    @NotNull(message = "interestRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "interestRate must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "interestRate must be <= 100")
    @Column(nullable = false)
    private Double interestRate;

    @NotNull(message = "startDate is required")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    @Column(nullable = false)
    private LocalDate endDate;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @NotNull(message = "owner is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @PrePersist
    void onCreate() {
        if (collectedAmount == null) collectedAmount = 0.0;
        validateDates();
    }

    @PreUpdate
    void onUpdate() {
        validateDates();
    }

    private void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be after or equal to startDate");
        }
    }

    public enum ProjectStatus {
        DRAFT, ACTIVE, FUNDED, CLOSED
    }
}
