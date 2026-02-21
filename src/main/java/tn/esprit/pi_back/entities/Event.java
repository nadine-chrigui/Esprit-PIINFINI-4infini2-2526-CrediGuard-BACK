package tn.esprit.pi_back.entities;

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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String eventType; // e.g. FORMATION, STAND, ATELIER

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateStart;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateEnd;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String location;

    @NotNull
    @Column(nullable = false)
    private Integer capacity;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String status; // PLANIFIE, OUVERT_INSCRIPTION, COMPLET, TERMINE, ANNULE

    // Optionnel : organisateur partenaire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;
}
