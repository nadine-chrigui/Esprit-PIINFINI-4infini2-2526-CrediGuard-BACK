package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "fullName is required")
    @Size(min = 2, max = 120)
    @Column(nullable = false, length = 120)
    private String fullName;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "phone must contain 8 digits")
    @Column(nullable = false, length = 20)
    private String phone;

    @NotBlank(message = "city is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "addressLine is required")
    @Size(min = 5, max = 255)
    @Column(nullable = false, length = 255)
    private String addressLine;

    @Size(max = 255)
    private String additionalInfo; // étage, code porte...
}
