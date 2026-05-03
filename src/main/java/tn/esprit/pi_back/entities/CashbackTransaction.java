package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.CashbackTransactionType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashbackTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loyalty_account_id")
    @JsonIgnore
    private LoyaltyAccount loyaltyAccount;

    private double amount;
    private String description;
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    private CashbackTransactionType type;
}