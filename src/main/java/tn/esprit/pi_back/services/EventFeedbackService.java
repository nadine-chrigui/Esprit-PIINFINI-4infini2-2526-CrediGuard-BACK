package tn.esprit.pi_back.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dtos.EventFeedbackSummary;
import tn.esprit.pi_back.dtos.FeedbackDTO;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventFeedback;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.EventFeedbackRepository;
import tn.esprit.pi_back.repositories.EventRegistrationRepository;
import tn.esprit.pi_back.repositories.EventRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class EventFeedbackService {

    @Autowired
    private EventFeedbackRepository feedbackRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    public EventFeedback submitFeedback(Long eventId, Long userId, FeedbackDTO feedbackDTO) {
        // Vérifier que l'utilisateur a participé à l'événement
        boolean attended = registrationRepository.existsByEventIdAndUserId(eventId, userId);
        if (!attended) {
            throw new IllegalArgumentException("Vous devez avoir participé à l'événement pour donner un feedback");
        }

        // Vérifier que l'utilisateur n'a pas déjà donné un feedback
        Long existingFeedbackCount = feedbackRepository.countByEventIdAndUserId(eventId, userId);
        if (existingFeedbackCount > 0) {
            throw new IllegalArgumentException("Vous avez déjà donné un feedback pour cet événement");
        }

        // Récupérer l'événement et l'utilisateur
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé avec l'ID: " + eventId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        // Créer le feedback
        EventFeedback feedback = new EventFeedback();
        feedback.setEvent(event);
        feedback.setUser(user);
        feedback.setOverallRating(feedbackDTO.getOverallRating());
        feedback.setOrganizationRating(feedbackDTO.getOrganizationRating());
        feedback.setContentRating(feedbackDTO.getContentRating());
        feedback.setVenueRating(feedbackDTO.getVenueRating());
        feedback.setValueRating(feedbackDTO.getValueRating());
        feedback.setParticipationRating(feedbackDTO.getParticipationRating());
        feedback.setComment(feedbackDTO.getComment());
        feedback.setSuggestions(feedbackDTO.getSuggestions());
        feedback.setWouldRecommend(feedbackDTO.getWouldRecommend());
        feedback.setFeedbackDate(LocalDateTime.now());
        feedback.setVerifiedAttendance(true);

        return feedbackRepository.save(feedback);
    }

    public EventFeedbackSummary getFeedbackSummary(Long eventId) {
        List<EventFeedback> feedbacks = feedbackRepository.findByEventId(eventId);
        
        if (feedbacks.isEmpty()) {
            return null;
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        
        EventFeedbackSummary summary = EventFeedbackSummary.builder()
                .eventId(eventId)
                .eventName(event != null ? event.getTitle() : "Événement inconnu")
                .totalFeedbacks(feedbacks.size())
                .averageOverallRating(calculateAverage(feedbacks, EventFeedback::getOverallRating))
                .averageOrganizationRating(calculateAverage(feedbacks, EventFeedback::getOrganizationRating))
                .averageContentRating(calculateAverage(feedbacks, EventFeedback::getContentRating))
                .averageVenueRating(calculateAverage(feedbacks, EventFeedback::getVenueRating))
                .averageValueRating(calculateAverage(feedbacks, EventFeedback::getValueRating))
                .averageParticipationRating(calculateAverage(feedbacks, EventFeedback::getParticipationRating))
                .recommendationRate(calculateRecommendationRate(feedbacks))
                .recentFeedbacks(getRecentFeedbacks(feedbacks, 5))
                .lastUpdated(LocalDateTime.now())
                .build();

        return summary;
    }

    public List<EventFeedback> getUserFeedbackForEvent(Long eventId, Long userId) {
        return feedbackRepository.findByEventId(eventId).stream()
                .filter(f -> f.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public boolean canUserGiveFeedback(Long eventId, Long userId) {
        // Vérifier si l'utilisateur a participé
        boolean attended = registrationRepository.existsByEventIdAndUserId(eventId, userId);
        if (!attended) {
            return false;
        }

        // Vérifier si l'utilisateur a déjà donné un feedback
        Long existingFeedbackCount = feedbackRepository.countByEventIdAndUserId(eventId, userId);
        return existingFeedbackCount == 0;
    }

    private Double calculateAverage(List<EventFeedback> feedbacks, Function<EventFeedback, Integer> getter) {
        return feedbacks.stream()
                .map(feedback -> {
                    Integer value = getter.apply(feedback);
                    return value != null ? value : 0;
                })
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private Double calculateRecommendationRate(List<EventFeedback> feedbacks) {
        long recommendations = feedbacks.stream()
                .mapToLong(f -> f.getWouldRecommend() != null && f.getWouldRecommend() ? 1 : 0)
                .sum();
        return feedbacks.size() > 0 ? (double) recommendations / feedbacks.size() * 100 : 0.0;
    }

    private List<EventFeedback> getRecentFeedbacks(List<EventFeedback> feedbacks, int limit) {
        return feedbacks.stream()
                .sorted((a, b) -> b.getFeedbackDate().compareTo(a.getFeedbackDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<EventFeedbackSummary> getAllEventsFeedbackSummaries() {
        List<Event> allEvents = eventRepository.findAll();
        
        return allEvents.stream()
                .map(event -> getFeedbackSummary(event.getId()))
                .filter(summary -> summary != null && summary.getTotalFeedbacks() > 0)
                .collect(Collectors.toList());
    }
}
