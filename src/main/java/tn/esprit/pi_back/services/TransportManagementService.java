package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.TransportBookingRequestDto;
import tn.esprit.pi_back.dto.TransportBookingResponseDto;
import tn.esprit.pi_back.dto.TransportServiceRequestDto;
import tn.esprit.pi_back.dto.TransportServiceResponseDto;

import java.util.List;

/**
 * Service de gestion du transport (TransportService + TransportBooking).
 * On évite le nom "TransportService" pour ne pas entrer en conflit avec l'entité.
 */
public interface TransportManagementService {

    // TransportService CRUD
    TransportServiceResponseDto createTransportService(TransportServiceRequestDto transportService);
    TransportServiceResponseDto updateTransportService(Long id, TransportServiceRequestDto transportService);
    TransportServiceResponseDto getTransportServiceById(Long id);
    List<TransportServiceResponseDto> getAllTransportServices();
    void deleteTransportService(Long id);

    // TransportBooking CRUD
    TransportBookingResponseDto createBooking(TransportBookingRequestDto booking);
    TransportBookingResponseDto updateBooking(Long id, TransportBookingRequestDto booking);
    TransportBookingResponseDto getBookingById(Long id);
    List<TransportBookingResponseDto> getAllBookings();
    void deleteBooking(Long id);
}
