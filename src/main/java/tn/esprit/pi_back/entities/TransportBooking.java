package tn.esprit.pi_back.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class TransportBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transport_service_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TransportService transportService;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime bookingDate;

    private Integer seatNumber;

    @NotNull
    @Column(nullable = false, length = 50)
    private String bookingStatus; // RESERVE, CONFIRME, ANNULE, NO_SHOW
}
