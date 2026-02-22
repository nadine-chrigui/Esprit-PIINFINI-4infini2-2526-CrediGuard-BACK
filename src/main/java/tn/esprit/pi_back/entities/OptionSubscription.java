package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "purchaseOption"})
public class OptionSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long subscriptionId;

    @NotNull(message = "reservedQuantity is required")
    @Min(value = 1, message = "reservedQuantity must be >= 1")
    @Column(nullable = false)
    private Integer reservedQuantity;

    @NotNull(message = "subscriptionDate is required")
    @Column(nullable = false)
    private LocalDate subscriptionDate;

    @NotNull(message = "amountPaid is required")
    @PositiveOrZero(message = "amountPaid must be >= 0")
    @Column(nullable = false)
    private Double amountPaid;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.RESERVED;

    @NotNull(message = "user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "purchaseOption is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private PurchaseOption purchaseOption;

    // 1 subscription -> 0..1 redemption (mappedBy dans OptionRedemption)
    @OneToOne(mappedBy = "subscription", fetch = FetchType.LAZY)
    private OptionRedemption redemption;

    public enum SubscriptionStatus {
        RESERVED, PAID, CANCELLED
    }
}