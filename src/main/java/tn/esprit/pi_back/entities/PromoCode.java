package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DiscountType;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /* ================= CORE ================= */

    @NotBlank(message = "code is required")
    @Size(min = 3, max = 30, message = "code must be between 3 and 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String code; // ex: WELCOME10

    @NotNull(message = "discountType is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENTAGE / FIXED (selon ton enum)

    @NotNull(message = "discountValue is required")
    @Positive(message = "discountValue must be > 0")
    @Column(nullable = false)
    private Double discountValue;

    /* ================= RULES ================= */

    @NotNull(message = "active is required")
    @Column(nullable = false)
    private Boolean active = true;

    @NotNull(message = "maxUses is required")
    @Positive(message = "maxUses must be > 0")
    @Column(nullable = false)
    private Integer maxUses = 1;

    @NotNull(message = "usedCount is required")
    @PositiveOrZero(message = "usedCount must be >= 0")
    @Column(nullable = false)
    private Integer usedCount = 0;

    @PositiveOrZero(message = "minOrderAmount must be >= 0")
    private Double minOrderAmount; // ex: 100 DT

    @PositiveOrZero(message = "maxDiscountAmount must be >= 0")
    private Double maxDiscountAmount; // utile si discountType=PERCENTAGE

    /* ================= VALIDITY ================= */

    private LocalDateTime startAt; // date début validité
    private LocalDateTime endAt;   // date fin validité

    /* ================= AUDIT ================= */

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (maxUses == null) maxUses = 1;
        if (usedCount == null) usedCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
