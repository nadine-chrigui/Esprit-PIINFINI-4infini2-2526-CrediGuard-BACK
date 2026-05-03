package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.LoyaltyLevel;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loyalty_account_id")
    @JsonIgnore
    private LoyaltyAccount loyaltyAccount;

    @Enumerated(EnumType.STRING)
    private LoyaltyLevel previousLevel;

    @Enumerated(EnumType.STRING)
    private LoyaltyLevel newLevel;

    private LocalDateTime changeDate;
}
