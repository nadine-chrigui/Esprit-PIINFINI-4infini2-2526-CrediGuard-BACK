package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_feedback")
public class EventFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Évaluation globale
    @Column(name = "overall_rating", nullable = false)
    @Min(1) @Max(5)
    @NotNull
    private Integer overallRating;

    // Critères détaillés
    @Column(name = "organization_rating")
    @Min(1) @Max(5)
    private Integer organizationRating;

    @Column(name = "content_rating")
    @Min(1) @Max(5)
    private Integer contentRating;

    @Column(name = "venue_rating")
    @Min(1) @Max(5)
    private Integer venueRating;

    @Column(name = "value_rating")
    @Min(1) @Max(5)
    private Integer valueRating;

    @Column(name = "participation_rating")
    @Min(1) @Max(5)
    private Integer participationRating;

    // Commentaires
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    // Recommandation
    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    // Métadonnées
    @Column(name = "feedback_date")
    private LocalDateTime feedbackDate;

    @Column(name = "verified_attendance")
    private Boolean verifiedAttendance;

    public EventFeedback() {
        this.feedbackDate = LocalDateTime.now();
        this.verifiedAttendance = false;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getOrganizationRating() {
        return organizationRating;
    }

    public void setOrganizationRating(Integer organizationRating) {
        this.organizationRating = organizationRating;
    }

    public Integer getContentRating() {
        return contentRating;
    }

    public void setContentRating(Integer contentRating) {
        this.contentRating = contentRating;
    }

    public Integer getVenueRating() {
        return venueRating;
    }

    public void setVenueRating(Integer venueRating) {
        this.venueRating = venueRating;
    }

    public Integer getValueRating() {
        return valueRating;
    }

    public void setValueRating(Integer valueRating) {
        this.valueRating = valueRating;
    }

    public Integer getParticipationRating() {
        return participationRating;
    }

    public void setParticipationRating(Integer participationRating) {
        this.participationRating = participationRating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean getWouldRecommend() {
        return wouldRecommend;
    }

    public void setWouldRecommend(Boolean wouldRecommend) {
        this.wouldRecommend = wouldRecommend;
    }

    public LocalDateTime getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(LocalDateTime feedbackDate) {
        this.feedbackDate = feedbackDate;
    }

    public Boolean getVerifiedAttendance() {
        return verifiedAttendance;
    }

    public void setVerifiedAttendance(Boolean verifiedAttendance) {
        this.verifiedAttendance = verifiedAttendance;
    }
}
