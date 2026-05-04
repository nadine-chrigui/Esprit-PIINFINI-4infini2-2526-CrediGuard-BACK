package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventRegistration;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.EventRegistrationRepository;
import tn.esprit.pi_back.repositories.EventRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;

    @Transactional
    public void registerUser(String email, Long eventId) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        if (event.getCapacity() == null || event.getCapacity() <= 0) {
            throw new IllegalStateException("Cet événement n'a pas de capacité définie");
        }

        long currentRegistrations = registrationRepo.countByEventId(eventId);
        if (currentRegistrations >= event.getCapacity()) {
            throw new IllegalStateException("Plus de places disponibles pour cet événement");
        }

        if (registrationRepo.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new IllegalStateException("Vous êtes déjà inscrit à cet événement");
        }

        EventRegistration registration = new EventRegistration();
        registration.setUser(user);
        registration.setEvent(event);
        registrationRepo.save(registration);
    }

    @Transactional
    public void unregisterUser(String email, Long eventId) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        boolean exists = registrationRepo.existsByUserIdAndEventId(user.getId(), eventId);
        if (!exists) {
            throw new IllegalStateException("Vous n'êtes pas inscrit à cet événement");
        }
        registrationRepo.deleteByUserIdAndEventId(user.getId(), eventId);
    }

    public List<Event> getEventsRegisteredByUser(String email) {
        System.out.println("DEBUG: getEventsRegisteredByUser for email = " + email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        System.out.println("DEBUG: user.id = " + user.getId());
        List<Event> events = registrationRepo.findEventsByUserId(user.getId());
        System.out.println("DEBUG: events found = " + events.size());
        return events;
    }

    public boolean isUserRegistered(String email, Long eventId) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        return registrationRepo.existsByUserIdAndEventId(user.getId(), eventId);
    }
}
