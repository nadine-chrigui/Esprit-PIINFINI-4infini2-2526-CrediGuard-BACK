package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.PaymentType;
import tn.esprit.pi_back.entities.enums.PricingStrategy;
import tn.esprit.pi_back.entities.enums.SaleMode;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"seller", "category"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "seller is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @NotNull(message = "category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank(message = "name is required")
    @Size(min = 2, max = 120, message = "name must be between 2 and 120 characters")
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 2000, message = "description too long")
    @Column(length = 2000)
    private String description;

    /* ================= PRICING ================= */

    @NotNull(message = "basePrice is required")
    @Positive(message = "basePrice must be > 0")
    @Column(nullable = false)
    private Double basePrice;

    @Positive(message = "currentPrice must be > 0")
    private Double currentPrice;

    @Column(nullable = false)
    private boolean dynamicPricingEnabled = false;

    @Enumerated(EnumType.STRING)
    private PricingStrategy pricingStrategy;

    /* ================= SALE TYPE ================= */

    @NotNull(message = "saleType is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleMode saleType;

    /* ================= STOCK / PREORDER ================= */

    @PositiveOrZero(message = "stockQuantity must be >= 0")
    private Integer stockQuantity;

    @Positive(message = "preorderQuota must be > 0")
    private Integer preorderQuota;

    @PositiveOrZero(message = "preorderCount must be >= 0")
    private Integer preorderCount;

    /* ================= PAYMENT ================= */

    @Enumerated(EnumType.STRING)
    private PaymentType paymentMode;

    @DecimalMin(value = "0.0", inclusive = false, message = "depositPercentage must be > 0")
    @DecimalMax(value = "1.0", message = "depositPercentage must be <= 1 (ex: 0.2 = 20%)")
    private Double depositPercentage;

    /* ================= DELIVERY ================= */

    @Column(nullable = false)
    private boolean expressDeliveryAvailable = false;

    @PositiveOrZero(message = "expressDeliveryFee must be >= 0")
    private Double expressDeliveryFee;

    /* ================= PREORDER DATES ================= */

    private LocalDateTime preorderStartDate;
    private LocalDateTime preorderEndDate;
    private LocalDateTime expectedReleaseDate;

    /* ================= STATUS ================= */

    @Column(nullable = false)
    private boolean active = true;

    /* ================= AUDIT ================= */

    @Column(length = 500)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (currentPrice == null) currentPrice = basePrice;
        if (preorderCount == null) preorderCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (currentPrice == null) currentPrice = basePrice;
    }
}
