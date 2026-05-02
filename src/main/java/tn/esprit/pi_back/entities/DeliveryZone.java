package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DeliveryZoneRisk;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DeliveryZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 100)
    private String governorate;

    @Size(max = 100)
    private String delegation;

    @Size(max = 120)
    private String locality;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryZoneRisk riskLevel = DeliveryZoneRisk.NORMAL;

    @PositiveOrZero
    private Double feeAdjustment = 0.0;

    @PositiveOrZero
    private Integer extraDelayDays = 0;

    private Boolean requiresAdminApproval = false;

    private Boolean active = true;

    @Size(max = 255)
    private String reason;

    @NotBlank
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String geoJsonPolygon;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (riskLevel == null) riskLevel = DeliveryZoneRisk.NORMAL;
        if (feeAdjustment == null) feeAdjustment = 0.0;
        if (extraDelayDays == null) extraDelayDays = 0;
        if (requiresAdminApproval == null) requiresAdminApproval = false;
        if (active == null) active = true;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
