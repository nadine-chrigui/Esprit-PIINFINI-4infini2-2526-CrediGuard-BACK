package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // CONSEIL_BUDGET, ALERTE_DEPENSE, OPPORTUNITE

    @Column(columnDefinition = "TEXT")
    private String message;

    private Double potentialSavings;

    private boolean viewed = false;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
