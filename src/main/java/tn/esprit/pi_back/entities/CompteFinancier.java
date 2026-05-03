package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.CompteType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CompteFinancier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompte;

    @Column(nullable = false)
    private Double solde = 0.0;

    private Double creditLimite = 0.0;
    private Double creditUtilise = 0.0;
    private Double creditDisponible = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompteType typeCompte;

    // ── Loyalty and Card ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loyalty_level")
    private LoyaltyLevel loyaltyLevel;

    @OneToOne(mappedBy = "compte", cascade = CascadeType.ALL)
    private Carte carte;

    // ── Personal info filled in by the front-office form ──
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String idType;
    private String idFileName;

    // ── JPA relationship: excluded from JSON to avoid Hibernate proxy issues ──
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private User utilisateur;

    // ── Transient: populated from the JPA relation on load, accepted on write ──
    @Transient
    private Long utilisateurId;

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @PostLoad
    protected void onPostLoad() {
        if (this.utilisateur != null) {
            this.utilisateurId = this.utilisateur.getId();
        }
    }
}
