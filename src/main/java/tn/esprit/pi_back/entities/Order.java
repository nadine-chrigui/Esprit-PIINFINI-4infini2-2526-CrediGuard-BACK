package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import tn.esprit.pi_back.entities.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "items"})
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /* ================= BUYER ================= */

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* ================= STATUS ================= */

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    /* ================= TOTAL ================= */

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Double totalAmount = 0.0;

    /* ================= PROMO ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    /* ================= ITEMS ================= */

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();


    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    /* ================= FINANCE LINK ================= */

    // Référence de la transaction créée dans le module financier
    // Exemple: TXN-2026-001
    @Column(unique = true)
    private String financeReference;

    /* ================= AUDIT ================= */

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) status = OrderStatus.PENDING;
        if (totalAmount == null) totalAmount = 0.0;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
