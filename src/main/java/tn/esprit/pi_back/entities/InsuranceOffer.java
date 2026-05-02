package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InsuranceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private Double annualPremium;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String coverageDetails;

    @Column(length = 1000)
    private String guarantees;

    @Column(length = 1000)
    private String exclusions;

    @NotBlank
    private String type; // e.g., TRANSPORT, BIENS, VIE

    private Double coverageAmount;
    private Double franchise;
    private Integer coverageRate;

    private String tags;

    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private InsuranceCompany insuranceCompany;
}
