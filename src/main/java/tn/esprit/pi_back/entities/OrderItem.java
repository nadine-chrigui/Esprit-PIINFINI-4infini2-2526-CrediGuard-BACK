package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "product"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be >= 1")
    @Column(nullable = false)
    private Integer quantity;

    // prix au moment de l’achat
    @NotNull(message = "unitPrice is required")
    @Positive(message = "unitPrice must be > 0")
    @Column(nullable = false)
    private Double unitPrice;

    // total ligne (quantity * unitPrice)
    @NotNull(message = "lineTotal is required")
    @Positive(message = "lineTotal must be > 0")
    @Column(nullable = false)
    private Double lineTotal;

    @PrePersist
    void onCreate() {
        if (lineTotal == null && unitPrice != null && quantity != null) {
            lineTotal = unitPrice * quantity;
        }
    }
}
