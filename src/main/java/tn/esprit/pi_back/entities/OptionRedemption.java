package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"subscription", "order", "transaction"})
public class OptionRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long redemptionId;

    @NotNull(message = "redeemedQuantity is required")
    @Min(value = 1, message = "redeemedQuantity must be >= 1")
    @Column(nullable = false)
    private Integer redeemedQuantity;

    @NotNull(message = "redemptionDate is required")
    @Column(nullable = false)
    private LocalDate redemptionDate;

    @NotNull(message = "finalPrice is required")
    @Positive(message = "finalPrice must be > 0")
    @Column(nullable = false)
    private Double finalPrice;

    @NotNull(message = "commissionAmount is required")
    @PositiveOrZero(message = "commissionAmount must be >= 0")
    @Column(nullable = false)
    private Double commissionAmount;

    // subscription (1) -> (0..1) redemption
    @NotNull(message = "subscription is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private OptionSubscription subscription;

    // ✅ UNIQUEMENT Order et Transaction
    @NotNull(message = "order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "transaction is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;
}
