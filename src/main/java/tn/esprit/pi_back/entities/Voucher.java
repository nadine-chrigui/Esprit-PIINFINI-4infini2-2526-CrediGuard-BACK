package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.VoucherStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherStatus status = VoucherStatus.ACTIVE;

    private LocalDate expirationDate;

    // ✅ بدل Beneficiary : voucher appartient à un User CLIENT
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_credit_id", unique = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private DemandeCredit demandeCredit;

    // 1 voucher -> 0..1 transaction
    @OneToOne(mappedBy = "voucher", fetch = FetchType.LAZY)
    private Transaction transaction;

    // 1 voucher -> * claims (Refunds)
    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    private java.util.List<InsuranceClaim> insuranceClaims;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = VoucherStatus.ACTIVE;
    }
}
