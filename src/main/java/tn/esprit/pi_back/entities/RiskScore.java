package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer globalScore;
    private Integer sectorScore;
    private Integer regionScore;
    private Integer historyScore;

    @Column(columnDefinition = "TEXT")
    private String factorDetails; // Stored as JSON string

    private LocalDateTime computedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        computedAt = LocalDateTime.now();
    }
}
