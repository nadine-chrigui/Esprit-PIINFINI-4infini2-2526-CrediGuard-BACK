package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.LoyaltyLevel;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private LoyaltyLevel currentLevel;

    private double cashbackBalance;
    private int onTimePayments;
    private int accountAgeMonths;

    @OneToMany(mappedBy = "loyaltyAccount", cascade = CascadeType.ALL)
    private List<CashbackTransaction> transactions;

    @OneToMany(mappedBy = "loyaltyAccount", cascade = CascadeType.ALL)
    private List<LoyaltyHistory> history;
}
