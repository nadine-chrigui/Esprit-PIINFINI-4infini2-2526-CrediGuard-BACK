package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    @FutureOrPresent
    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @NotNull
    @FutureOrPresent
    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Size(max = 1000)
    @Column(columnDefinition = "varchar(1000)")
    private String description;

    @NotBlank
    @Size(max = 255)
    @Column(name = "event_type")
    private String eventType;

    @NotBlank
    @Size(max = 255)
    private String location;

    @NotBlank
    @Size(max = 255)
    private String status;

    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    // Champs financiers
    @Column(name = "ticket_price", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal ticketPrice = BigDecimal.valueOf(50.0);

    @Column(name = "sponsorship_amount", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal sponsorshipAmount = BigDecimal.ZERO;

    @Column(name = "venue_cost", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal venueCost = BigDecimal.ZERO;

    @Column(name = "marketing_cost", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal marketingCost = BigDecimal.ZERO;

    @Column(name = "staff_cost", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal staffCost = BigDecimal.ZERO;

    @Column(name = "equipment_cost", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal equipmentCost = BigDecimal.ZERO;

    @Column(name = "budget_estimated", precision = 10, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal budgetEstimated = BigDecimal.ZERO;

    public Event() {
    }

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    // Getters et setters pour les champs financiers
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public BigDecimal getSponsorshipAmount() {
        return sponsorshipAmount;
    }

    public void setSponsorshipAmount(BigDecimal sponsorshipAmount) {
        this.sponsorshipAmount = sponsorshipAmount;
    }

    public BigDecimal getVenueCost() {
        return venueCost;
    }

    public void setVenueCost(BigDecimal venueCost) {
        this.venueCost = venueCost;
    }

    public BigDecimal getMarketingCost() {
        return marketingCost;
    }

    public void setMarketingCost(BigDecimal marketingCost) {
        this.marketingCost = marketingCost;
    }

    public BigDecimal getStaffCost() {
        return staffCost;
    }

    public void setStaffCost(BigDecimal staffCost) {
        this.staffCost = staffCost;
    }

    public BigDecimal getEquipmentCost() {
        return equipmentCost;
    }

    public void setEquipmentCost(BigDecimal equipmentCost) {
        this.equipmentCost = equipmentCost;
    }

    public BigDecimal getBudgetEstimated() {
        return budgetEstimated;
    }

    public void setBudgetEstimated(BigDecimal budgetEstimated) {
        this.budgetEstimated = budgetEstimated;
    }
}
