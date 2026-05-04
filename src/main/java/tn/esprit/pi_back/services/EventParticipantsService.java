package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventRegistration;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.EventRegistrationRepository;
import tn.esprit.pi_back.repositories.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventParticipantsService {

    private final EventRegistrationRepository registrationRepo;
    private final EventRepository eventRepo;

    public List<User> getEventParticipants(Long eventId) {
        System.out.println("=== DEBUG getEventParticipants ===");
        System.out.println("eventId: " + eventId);
        
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));
        
        System.out.println("Event found: " + event.getTitle());
        
        List<EventRegistration> registrations = registrationRepo.findByEventId(eventId);
        System.out.println("Registrations found: " + registrations.size());
        
        List<User> participants = registrations.stream()
                .map(EventRegistration::getUser)
                .collect(Collectors.toList());
        
        System.out.println("Participants: " + participants.size());
        participants.forEach(p -> System.out.println("- " + p.getFullName() + " (" + p.getEmail() + ")"));
        
        return participants;
    }

    public long getParticipantsCount(Long eventId) {
        return registrationRepo.countByEventId(eventId);
    }

    public List<EventRegistration> getRegistrationDetails(Long eventId) {
        return registrationRepo.findByEventId(eventId);
    }
}
