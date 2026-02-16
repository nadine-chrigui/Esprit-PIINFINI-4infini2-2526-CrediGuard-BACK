package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DeliverySlot;
import tn.esprit.pi_back.entities.enums.DeliveryStatus;
import tn.esprit.pi_back.entities.enums.DeliveryType;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "address"})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /* ================= LINK ================= */

    @NotNull(message = "order is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotNull(message = "address is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private DeliveryAddress address;

    /* ================= DELIVERY INFO ================= */

    @NotNull(message = "deliveryType is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType; // STANDARD / EXPRESS (ton enum)

    @NotNull(message = "deliveryStatus is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private DeliverySlot deliverySlot; // MORNING/AFTERNOON/EVENING (selon ton enum)

    @PositiveOrZero(message = "deliveryFee must be >= 0")
    private Double deliveryFee;

    /* ================= SCHEDULING ================= */

    private LocalDateTime scheduledAt;      // date choisie
    private LocalDateTime shippedAt;        // date expédition
    private LocalDateTime deliveredAt;      // date livraison

    /* ================= TRACKING ================= */

    @Size(max = 80)
    private String trackingNumber;

    @Size(max = 200)
    private String carrier; // poste, aramex, etc.

    /* ================= AUDIT ================= */

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deliveryStatus == null) deliveryStatus = DeliveryStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
