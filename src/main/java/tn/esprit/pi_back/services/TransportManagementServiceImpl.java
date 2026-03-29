package tn.esprit.pi_back.services;

import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.TransportBookingRequestDto;
import tn.esprit.pi_back.dto.TransportBookingResponseDto;
import tn.esprit.pi_back.dto.TransportServiceRequestDto;
import tn.esprit.pi_back.dto.TransportServiceResponseDto;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.entities.TransportBooking;
import tn.esprit.pi_back.entities.TransportService;
import tn.esprit.pi_back.repositories.EventRepository;
import tn.esprit.pi_back.repositories.TransportBookingRepository;
import tn.esprit.pi_back.repositories.TransportServiceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransportManagementServiceImpl implements TransportManagementService {

    private final TransportServiceRepository transportServiceRepository;
    private final TransportBookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public TransportManagementServiceImpl(TransportServiceRepository transportServiceRepository,
                                          TransportBookingRepository bookingRepository,
                                          EventRepository eventRepository) {
        this.transportServiceRepository = transportServiceRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    // ===================== TransportService =====================

    @Override
    public TransportServiceResponseDto createTransportService(TransportServiceRequestDto transportService) {
        TransportService entity = new TransportService();
        applyTransportServiceRequest(entity, transportService);
        TransportService saved = transportServiceRepository.save(entity);
        return toTransportServiceResponse(saved);
    }

    @Override
    public TransportServiceResponseDto updateTransportService(Long id, TransportServiceRequestDto transportService) {
        TransportService existing = getTransportServiceEntityById(id);
        applyTransportServiceRequest(existing, transportService);
        TransportService saved = transportServiceRepository.save(existing);
        return toTransportServiceResponse(saved);
    }

    @Override
    public TransportServiceResponseDto getTransportServiceById(Long id) {
        return toTransportServiceResponse(getTransportServiceEntityById(id));
    }

    @Override
    public List<TransportServiceResponseDto> getAllTransportServices() {
        return transportServiceRepository.findAll().stream()
                .map(this::toTransportServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTransportService(Long id) {
        TransportService existing = getTransportServiceEntityById(id);
        transportServiceRepository.delete(existing);
    }

    // ===================== TransportBooking =====================

    @Override
    public TransportBookingResponseDto createBooking(TransportBookingRequestDto booking) {
        TransportBooking entity = new TransportBooking();
        applyBookingRequest(entity, booking);
        TransportBooking saved = bookingRepository.save(entity);
        return toBookingResponse(saved);
    }

    @Override
    public TransportBookingResponseDto updateBooking(Long id, TransportBookingRequestDto booking) {
        TransportBooking existing = getBookingEntityById(id);
        applyBookingRequest(existing, booking);
        TransportBooking saved = bookingRepository.save(existing);
        return toBookingResponse(saved);
    }

    @Override
    public TransportBookingResponseDto getBookingById(Long id) {
        return toBookingResponse(getBookingEntityById(id));
    }

    @Override
    public List<TransportBookingResponseDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBooking(Long id) {
        TransportBooking existing = getBookingEntityById(id);
        bookingRepository.delete(existing);
    }

    private Event getEventEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    private TransportService getTransportServiceEntityById(Long id) {
        return transportServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TransportService not found with id: " + id));
    }

    private TransportBooking getBookingEntityById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TransportBooking not found with id: " + id));
    }

    private void applyTransportServiceRequest(TransportService target, TransportServiceRequestDto source) {
        if (source.getEventId() == null) {
            throw new RuntimeException("Event ID is required");
        }
        Event event = getEventEntityById(source.getEventId());
        target.setEvent(event);

        target.setTransportType(source.getTransportType());
        target.setDeparturePlace(source.getDeparturePlace());
        target.setDepartureTime(source.getDepartureTime());
        target.setReturnTime(source.getReturnTime());
        target.setCapacity(source.getCapacity());
        target.setStatus(source.getStatus());
    }

    private TransportServiceResponseDto toTransportServiceResponse(TransportService entity) {
        TransportServiceResponseDto dto = new TransportServiceResponseDto();
        dto.setId(entity.getId());
        dto.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        dto.setTransportType(entity.getTransportType());
        dto.setDeparturePlace(entity.getDeparturePlace());
        dto.setDepartureTime(entity.getDepartureTime());
        dto.setReturnTime(entity.getReturnTime());
        dto.setCapacity(entity.getCapacity());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private void applyBookingRequest(TransportBooking target, TransportBookingRequestDto source) {
        if (source.getTransportServiceId() == null) {
            throw new RuntimeException("TransportService ID is required");
        }
        TransportService transportService = getTransportServiceEntityById(source.getTransportServiceId());
        target.setTransportService(transportService);

        target.setBookingDate(source.getBookingDate());
        target.setSeatNumber(source.getSeatNumber());
        target.setBookingStatus(source.getBookingStatus());
    }

    private TransportBookingResponseDto toBookingResponse(TransportBooking entity) {
        TransportBookingResponseDto dto = new TransportBookingResponseDto();
        dto.setId(entity.getId());
        dto.setTransportServiceId(entity.getTransportService() != null ? entity.getTransportService().getId() : null);
        dto.setBookingDate(entity.getBookingDate());
        dto.setSeatNumber(entity.getSeatNumber());
        dto.setBookingStatus(entity.getBookingStatus());
        return dto;
    }
}
