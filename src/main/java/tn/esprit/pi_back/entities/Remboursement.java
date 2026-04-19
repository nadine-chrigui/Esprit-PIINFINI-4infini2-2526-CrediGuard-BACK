package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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

    @ManyToOne
    @JoinColumn(name = "id_credit")
    private Credit credit;

    @OneToOne
    @JoinColumn(name = "id_transaction")
    private Transaction transaction;

    @PrePersist
    protected void onCreate() {
        if (dateRemboursement == null) {
            dateRemboursement = LocalDateTime.now();
        }
    }
}
