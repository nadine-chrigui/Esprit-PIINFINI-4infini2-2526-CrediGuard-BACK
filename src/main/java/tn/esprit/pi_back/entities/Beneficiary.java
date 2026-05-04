package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 user (BENEFICIARY) -> 0..1 beneficiary profile
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Pattern(regexp = "^[0-9]{8}$", message = "cin must contain 8 digits")
    private String cin;

    @Size(max = 255)
    private String address;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    void onCreate() {
        if (active == null) active = true;
    }
}
