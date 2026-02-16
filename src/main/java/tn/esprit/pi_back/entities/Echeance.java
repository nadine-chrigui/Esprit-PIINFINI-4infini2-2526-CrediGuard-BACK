package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.StatutEcheance;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateEcheance;

    @NotNull(message = "capitalDu is required")
    @PositiveOrZero(message = "capitalDu must be >= 0")
    @Column(nullable = false)
    private Double capitalDu;

    @NotNull(message = "interetDu is required")
    @PositiveOrZero(message = "interetDu must be >= 0")
    @Column(nullable = false)
    private Double interetDu;

    @NotNull(message = "statut is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutEcheance statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", nullable = false)
    private Credit credit;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
