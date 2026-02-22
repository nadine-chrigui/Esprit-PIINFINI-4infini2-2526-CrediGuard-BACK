package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.PaymentSource;
import tn.esprit.pi_back.entities.Voucher;



import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"compteSource", "compteDestination"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaction;

    /* ================= TYPE ================= */

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType typeTransaction;

    /* ================= AMOUNT ================= */

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double montant;

    /* ================= DATE ================= */

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateTransaction;

    /* ================= STATUS ================= */

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatut statut;

    /* ================= COMPTES ================= */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte_source")
    private CompteFinancier compteSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte_destination")
    private CompteFinancier compteDestination;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="voucher_transaction_id", unique = true)
    private Voucher voucher;

    // ✅ Partner = User (userType = PARTNER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    // ✅ Qui paye ?
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentSource paymentSource; // CLIENT / INSURANCE

    /* ================= LIAISON E-COMMERCE ================= */

    // Référence de l'Order qui a déclenché cette transaction
    @Column(nullable = false)
    private Long orderId;

    /* ================= AUDIT ================= */

    @PrePersist
    protected void onCreate() {
        if (dateTransaction == null) {
            dateTransaction = LocalDateTime.now();
        }
        if (statut == null) {
            statut = TransactionStatut.PENDING;
        }
    }



}
