package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"project"})
public class PurchaseOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long optionId;

    @NotNull(message = "fixedPrice is required")
    @Positive(message = "fixedPrice must be > 0")
    @Column(nullable = false)
    private Double fixedPrice;

    @NotNull(message = "maxQuantity is required")
    @Min(value = 1, message = "maxQuantity must be >= 1")
    @Column(nullable = false)
    private Integer maxQuantity;

    // commission est ici ✅
    @NotNull(message = "commissionRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "commissionRate must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "commissionRate must be <= 100")
    @Column(nullable = false)
    private Double commissionRate;

    @NotNull(message = "expirationDate is required")
    @Column(nullable = false)
    private LocalDate expirationDate;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OptionStatus status = OptionStatus.ACTIVE;

    @NotNull(message = "project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CrowdfundingProject project;

    public enum OptionStatus {
        ACTIVE, EXPIRED, SOLD_OUT, DISABLED
    }
}
