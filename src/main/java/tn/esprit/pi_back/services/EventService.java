package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventParticipation;
import tn.esprit.pi_back.repositories.EventParticipationRepository;
import tn.esprit.pi_back.repositories.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository eventParticipationRepository;

    // ---- Event CRUD ----

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    public Event createEvent(Event event) {
        event.setId(null); // sécurité pour forcer la création
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updated) {
        Event existing = getEventById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setEventType(updated.getEventType());
        existing.setDateStart(updated.getDateStart());
        existing.setDateEnd(updated.getDateEnd());
        existing.setLocation(updated.getLocation());
        existing.setCapacity(updated.getCapacity());
        existing.setStatus(updated.getStatus());
        existing.setPartner(updated.getPartner());
        return eventRepository.save(existing);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    // ---- EventParticipation CRUD simplifié ----

    public List<EventParticipation> getParticipationsByEvent(Long eventId) {
        return eventParticipationRepository.findByEventId(eventId);
    }

    public List<EventParticipation> getParticipationsByBeneficiary(Long beneficiaryId) {
        return eventParticipationRepository.findByBeneficiaryId(beneficiaryId);
    }

    public EventParticipation createParticipation(EventParticipation participation) {
        participation.setId(null);
        return eventParticipationRepository.save(participation);
    }

    public void deleteParticipation(Long participationId) {
        if (!eventParticipationRepository.existsById(participationId)) {
            throw new RuntimeException("EventParticipation not found with id: " + participationId);
        }
        eventParticipationRepository.deleteById(participationId);
    }
}
