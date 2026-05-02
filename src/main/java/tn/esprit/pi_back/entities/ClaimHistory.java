package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode
public class ClaimHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private InsuranceClaim claim;

    @Enumerated(EnumType.STRING)
    private ClaimStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private ClaimStatus newStatus;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    private LocalDateTime changedAt;

    @PrePersist
    void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
