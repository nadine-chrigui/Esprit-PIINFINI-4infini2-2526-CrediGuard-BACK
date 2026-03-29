package tn.esprit.pi_back.services;

import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.EventParticipationRequestDto;
import tn.esprit.pi_back.dto.EventParticipationResponseDto;
import tn.esprit.pi_back.dto.EventRequestDto;
import tn.esprit.pi_back.dto.EventResponseDto;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.EventParticipation;
import tn.esprit.pi_back.repositories.EventParticipationRepository;
import tn.esprit.pi_back.repositories.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;

    public EventServiceImpl(EventRepository eventRepository,
                            EventParticipationRepository participationRepository) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
    }

    // ========================= Event =========================

    @Override
    public EventResponseDto createEvent(EventRequestDto event) {
        Event entity = new Event();
        applyEventRequest(entity, event);
        Event saved = eventRepository.save(entity);
        return toEventResponse(saved);
    }

    @Override
    public EventResponseDto updateEvent(Long id, EventRequestDto event) {
        Event existing = getEventEntityById(id);
        applyEventRequest(existing, event);
        Event saved = eventRepository.save(existing);
        return toEventResponse(saved);
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        return toEventResponse(getEventEntityById(id));
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEvent(Long id) {
        Event existing = getEventEntityById(id);
        eventRepository.delete(existing);
    }

    // ==================== EventParticipation ====================

    @Override
    public EventParticipationResponseDto createParticipation(EventParticipationRequestDto participation) {
        EventParticipation entity = new EventParticipation();
        applyParticipationRequest(entity, participation);
        EventParticipation saved = participationRepository.save(entity);
        return toParticipationResponse(saved);
    }

    @Override
    public EventParticipationResponseDto updateParticipation(Long id, EventParticipationRequestDto participation) {
        EventParticipation existing = getParticipationEntityById(id);
        applyParticipationRequest(existing, participation);
        EventParticipation saved = participationRepository.save(existing);
        return toParticipationResponse(saved);
    }

    @Override
    public EventParticipationResponseDto getParticipationById(Long id) {
        return toParticipationResponse(getParticipationEntityById(id));
    }

    @Override
    public List<EventParticipationResponseDto> getAllParticipations() {
        return participationRepository.findAll().stream()
                .map(this::toParticipationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteParticipation(Long id) {
        EventParticipation existing = getParticipationEntityById(id);
        participationRepository.delete(existing);
    }

    private Event getEventEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    private EventParticipation getParticipationEntityById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EventParticipation not found with id: " + id));
    }

    private void applyEventRequest(Event target, EventRequestDto source) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setEventType(source.getEventType());
        target.setDateStart(source.getDateStart());
        target.setDateEnd(source.getDateEnd());
        target.setLocation(source.getLocation());
        target.setCapacity(source.getCapacity());
        target.setStatus(source.getStatus());
    }

    private EventResponseDto toEventResponse(Event entity) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setEventType(entity.getEventType());
        dto.setDateStart(entity.getDateStart());
        dto.setDateEnd(entity.getDateEnd());
        dto.setLocation(entity.getLocation());
        dto.setCapacity(entity.getCapacity());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private void applyParticipationRequest(EventParticipation target, EventParticipationRequestDto source) {
        if (source.getEventId() == null) {
            throw new RuntimeException("Event ID is required");
        }
        Event event = getEventEntityById(source.getEventId());
        target.setEvent(event);

        target.setRegistrationDate(source.getRegistrationDate());
        target.setParticipationStatus(source.getParticipationStatus());
        target.setFeedback(source.getFeedback());
        target.setRating(source.getRating());
    }

    private EventParticipationResponseDto toParticipationResponse(EventParticipation entity) {
        EventParticipationResponseDto dto = new EventParticipationResponseDto();
        dto.setId(entity.getId());
        dto.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setParticipationStatus(entity.getParticipationStatus());
        dto.setFeedback(entity.getFeedback());
        dto.setRating(entity.getRating());
        return dto;
    }
}
