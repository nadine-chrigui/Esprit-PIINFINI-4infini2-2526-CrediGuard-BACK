package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PartnerPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String purchaseReference;

    @NotNull
    private Double totalAmount;

    @Column(columnDefinition = "TEXT")
    private String productNames; // Store product names as a comma-separated string for simplicity as requested

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "partner_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
