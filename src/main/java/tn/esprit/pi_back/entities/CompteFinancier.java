package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.CompteType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CompteFinancier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompte;

    @NotNull(message = "Solde is required")
    @Min(value = 0, message = "Solde must be positive")
    private Double solde;

    @NotNull(message = "Compte type is required")
    @Enumerated(EnumType.STRING)
    private CompteType typeCompte;

    @OneToOne
    @JoinColumn(name = "id_utilisateur")
    private User utilisateur;
}
