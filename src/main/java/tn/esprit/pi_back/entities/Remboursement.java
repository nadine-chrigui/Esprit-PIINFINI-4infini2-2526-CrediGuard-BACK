package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"credit", "transaction"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Remboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRemboursement;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double montant;

    @NotNull(message = "Reimbursement date is required")
    private LocalDateTime dateRemboursement;

    private String mode = "automatique";

    private String orderReference;

    // ── JPA relations – excluded from JSON to prevent Hibernate proxy issues ──
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credit")
    private Credit credit;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaction")
    private Transaction transaction;

    // ── Transient IDs: accepted from Angular, resolved to entities in the service ──
    @Transient private Long creditId;
    @Transient private Long transactionId;

    public void setCreditId(Long id)      { this.creditId      = id; }
    public void setTransactionId(Long id) { this.transactionId = id; }

    @PostLoad
    protected void onPostLoad() {
        if (this.credit      != null) this.creditId      = this.credit.getId();
        if (this.transaction != null) this.transactionId = this.transaction.getIdTransaction();
    }

    @PrePersist
    protected void onCreate() {
        if (dateRemboursement == null) dateRemboursement = LocalDateTime.now();
    }
}
