package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_tracking")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PerformanceTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "investment_offer_id", nullable = false)
    private InvestmentOffer investmentOffer;

    private LocalDateTime date;

    private double value;

    private double variation; // percentage from previous

    @PrePersist
    protected void onCreate() {
        if (date == null) date = LocalDateTime.now();
    }
}