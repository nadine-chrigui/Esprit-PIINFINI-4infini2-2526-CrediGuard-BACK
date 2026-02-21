package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime registrationDate;

    @NotNull
    @Column(nullable = false, length = 50)
    private String participationStatus; // INSCRIT, CONFIRME, PRESENT, ABSENT, ANNULE

    @Column(length = 1000)
    private String feedback;

    private Integer rating; // 1..5
}
