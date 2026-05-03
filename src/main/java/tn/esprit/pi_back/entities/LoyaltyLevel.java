package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class LoyaltyLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Bronze, Silver, Dark Premium, Elite, Holographic

    private Double cashbackRate; // 1.0, 2.0, 4.0, etc.

    private Double spendingThreshold; // Seuil pour passer au niveau suivant

    private String cardColor; // Hex code or name

    @Column(columnDefinition = "TEXT")
    private String benefits; // Description of benefits
}
