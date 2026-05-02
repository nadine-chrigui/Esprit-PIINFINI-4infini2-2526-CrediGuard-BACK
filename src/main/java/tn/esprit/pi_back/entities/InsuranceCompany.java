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
public class InsuranceCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String registrationNumber;

    private String logoUrl;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    private java.util.List<String> categories;

    private Float reliabilityNote;

    private boolean active = true;
}
