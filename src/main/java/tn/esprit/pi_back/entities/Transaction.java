package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.PaymentSource;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"compteSource", "compteDestination", "partner", "voucher"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType typeTransaction;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double montant;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateTransaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatut statut;

    // ── All JPA relations are @JsonIgnore — use transient IDs instead ──
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte_source")
    private CompteFinancier compteSource;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte_destination")
    private CompteFinancier compteDestination;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_transaction_id", unique = true)
    private Voucher voucher;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentSource paymentSource;

    private String marchand;
    private String categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carte")
    private Carte carte;

    @Column(nullable = false)
    private Long orderId;

    // ── Transient IDs: written from Angular, read back via @PostLoad ──
    @Transient private Long compteSourceId;
    @Transient private Long compteDestinationId;
    @Transient private Long partnerId;

    public void setCompteSourceId(Long id)      { this.compteSourceId      = id; }
    public void setCompteDestinationId(Long id) { this.compteDestinationId = id; }
    public void setPartnerId(Long id)           { this.partnerId            = id; }

    // ── Populate transient IDs after load so the DTO mapper can read them ──
    @PostLoad
    protected void onPostLoad() {
        if (this.compteSource      != null) this.compteSourceId      = this.compteSource.getIdCompte();
        if (this.compteDestination != null) this.compteDestinationId = this.compteDestination.getIdCompte();
        if (this.partner           != null) this.partnerId            = this.partner.getId();
    }

    @PrePersist
    protected void onCreate() {
        if (dateTransaction == null) dateTransaction = LocalDateTime.now();
        if (statut          == null) statut          = TransactionStatut.PENDING;
    }
}