package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
public class TransportService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String transportType; // BUS, MINIBUS, VOITURE, NAVETTE

    @NotBlank
    @Column(nullable = false, length = 255)
    private String departurePlace;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime departureTime;

    private LocalDateTime returnTime;

    @NotNull
    @Column(nullable = false)
    private Integer capacity;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String status; // PLANIFIE, OUVERT_RESERVATION, COMPLET, TERMINE, ANNULE
}
