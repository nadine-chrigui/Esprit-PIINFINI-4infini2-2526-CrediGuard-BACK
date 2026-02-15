package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.RegleType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegleRemboursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRegle;

    @NotNull(message = "Rule type is required")
    @Enumerated(EnumType.STRING)
    private RegleType typeRegle;

    @NotNull(message = "Value is required")
    @Positive(message = "Value must be positive")
    private Double valeur;

    @ManyToOne
    @JoinColumn(name = "id_credit")
    private Credit credit;
}
