package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Carte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    private String cardHolderName;

    private String cvv;

    private LocalDate expiryDate;

    private boolean active = true;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_compte")
    private CompteFinancier compte;

    @ManyToOne
    @JoinColumn(name = "id_loyalty_level")
    private LoyaltyLevel loyaltyLevel;
}
