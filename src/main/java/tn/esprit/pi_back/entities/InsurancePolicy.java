package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import tn.esprit.pi_back.entities.enums.PolicyStatus;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String policyNumber;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "insurance_company_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private InsuranceCompany insuranceCompany;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "offer_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private InsuranceOffer insuranceOffer;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private User client;

    @NotNull
    @Column(nullable = false)
    private PolicyStatus status; // ACTIF, SUSPENDU, EXPIRE, RESILIE

    private Double premiumAmount;
    private Double declaredValue;
    private String goodsDescription;
    private String pdfUrl;
    private Integer durationYears;
}
