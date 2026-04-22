package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventCreateRequest;
import tn.esprit.pi_back.dto.calendar_event.CalendarEventResponse;
import tn.esprit.pi_back.entities.CalendarEvent;
import tn.esprit.pi_back.mappers.CalendarEventMapper;
import tn.esprit.pi_back.repositories.CalendarEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarEventMapper calendarEventMapper;

    @Override
    public CalendarEventResponse create(CalendarEventCreateRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("endDate must be after or equal to startDate");
        }

        CalendarEvent event = CalendarEvent.builder()
                .name(request.name())
                .code(request.code().trim().toUpperCase())
                .description(request.description())
                .eventType(request.eventType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .recurringAnnually(request.recurringAnnually() != null ? request.recurringAnnually() : false)
                .active(request.active() != null ? request.active() : true)
                .build();

        return calendarEventMapper.toResponse(calendarEventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getAll() {
        return calendarEventRepository.findAll()
                .stream()
                .map(calendarEventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getActive() {
        return calendarEventRepository.findActiveEventsAt(LocalDateTime.now())
                .stream()
                .map(calendarEventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarEventResponse getById(Long id) {
        CalendarEvent event = calendarEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Calendar event not found: " + id));
        return calendarEventMapper.toResponse(event);
    }
}