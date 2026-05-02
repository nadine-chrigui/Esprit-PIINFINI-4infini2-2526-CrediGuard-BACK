package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 80)
    private String claimNumber;

    @Column(name = "claim_reference", length = 100, nullable = false)
    private String claimReference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    private List<String> documentsUrl;

    private Double amountRequested;
    private Double amountApproved;
    private String rejectionReason;
    
    private Integer fraudScore; 
    private Integer riskScore; 

    private LocalDateTime declaredAt;
    private LocalDateTime decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private InsurancePolicy insurancePolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @PrePersist
    void onCreate() {
        declaredAt = LocalDateTime.now();
        if (status == null) status = ClaimStatus.PENDING;
    }
}
