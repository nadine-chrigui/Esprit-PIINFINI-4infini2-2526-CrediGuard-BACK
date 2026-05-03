package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Cashback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private Double appliedRate;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "id_transaction")
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "id_compte")
    private CompteFinancier compte;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
