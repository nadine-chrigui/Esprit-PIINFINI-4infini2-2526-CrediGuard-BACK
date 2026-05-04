package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ProfileType;

import java.time.LocalDateTime;

@Entity
@Table(name = "financial_profile")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FinancialProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double score;

    @Enumerated(EnumType.STRING)
    private ProfileType profileType;

    private double savingsRate;

    private double repaymentRate;

    private double historyScore;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}