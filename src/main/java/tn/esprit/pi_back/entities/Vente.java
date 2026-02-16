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
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVente;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double montantTotal;

    @NotNull(message = "Sale date is required")
    private LocalDateTime dateVente;

    @PrePersist
    protected void onCreate() {
        if (dateVente == null) {
            dateVente = LocalDateTime.now();
        }
    }
}
