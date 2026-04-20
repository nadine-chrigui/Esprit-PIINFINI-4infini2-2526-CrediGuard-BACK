package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.DiscountType;
import tn.esprit.pi_back.entities.enums.PromotionTargetType;
import tn.esprit.pi_back.entities.enums.PromotionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionType promotionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscountType discountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionTargetType targetType;

    @Column(nullable = false)
    private Double discountValue;

    private Double minOrderAmount;

    private Double maxDiscountAmount;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false)
    private Boolean autoApply = true;

    @Column(nullable = false)
    private Boolean stackable = false;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_event_id")
    private CalendarEvent calendarEvent;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) this.active = true;
        if (this.priority == null) this.priority = 0;
        if (this.autoApply == null) this.autoApply = true;
        if (this.stackable == null) this.stackable = false;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}