package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.EventParticipationRequestDto;
import tn.esprit.pi_back.dto.EventParticipationResponseDto;
import tn.esprit.pi_back.dto.EventRequestDto;
import tn.esprit.pi_back.dto.EventResponseDto;

import java.util.List;

public interface EventService {

    // Event CRUD
    EventResponseDto createEvent(EventRequestDto event);
    EventResponseDto updateEvent(Long id, EventRequestDto event);
    EventResponseDto getEventById(Long id);
    List<EventResponseDto> getAllEvents();
    void deleteEvent(Long id);

    // EventParticipation CRUD
    EventParticipationResponseDto createParticipation(EventParticipationRequestDto participation);
    EventParticipationResponseDto updateParticipation(Long id, EventParticipationRequestDto participation);
    EventParticipationResponseDto getParticipationById(Long id);
    List<EventParticipationResponseDto> getAllParticipations();
    void deleteParticipation(Long id);
}
