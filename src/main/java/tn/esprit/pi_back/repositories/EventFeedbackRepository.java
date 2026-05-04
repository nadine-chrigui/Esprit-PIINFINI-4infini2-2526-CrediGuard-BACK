package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.EventFeedback;
import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Long> {
    
    List<EventFeedback> findByEventId(Long eventId);
    
    List<EventFeedback> findByUserId(Long userId);
    
    @Query("SELECT COUNT(f) FROM EventFeedback f WHERE f.event.id = :eventId AND f.user.id = :userId")
    Long countByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
    
    @Query("SELECT AVG(f.overallRating) FROM EventFeedback f WHERE f.event.id = :eventId")
    Double getAverageRatingForEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT AVG(f.organizationRating) FROM EventFeedback f WHERE f.event.id = :eventId")
    Double getAverageOrganizationRatingForEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT AVG(f.contentRating) FROM EventFeedback f WHERE f.event.id = :eventId")
    Double getAverageContentRatingForEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT AVG(f.venueRating) FROM EventFeedback f WHERE f.event.id = :eventId")
    Double getAverageVenueRatingForEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT AVG(f.valueRating) FROM EventFeedback f WHERE f.event.id = :eventId")
    Double getAverageValueRatingForEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT COUNT(f) FROM EventFeedback f WHERE f.event.id = :eventId AND f.wouldRecommend = true")
    Long getRecommendationCount(@Param("eventId") Long eventId);
    
    @Query("SELECT f FROM EventFeedback f WHERE f.event.id = :eventId ORDER BY f.feedbackDate DESC")
    List<EventFeedback> findRecentFeedbacksByEventId(@Param("eventId") Long eventId);
    
    @Query("SELECT f FROM EventFeedback f WHERE f.comment IS NOT NULL AND LENGTH(f.comment) > 10 ORDER BY f.feedbackDate DESC")
    List<EventFeedback> findFeedbacksWithComments();
}
