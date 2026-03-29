package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.TransportBooking;
import tn.esprit.pi_back.entities.TransportService;
import tn.esprit.pi_back.repositories.TransportBookingRepository;
import tn.esprit.pi_back.repositories.TransportServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportServiceManager {

    private final TransportServiceRepository transportServiceRepository;
    private final TransportBookingRepository transportBookingRepository;

    // ---- TransportService CRUD ----

    public List<TransportService> getAllTransportServices() {
        return transportServiceRepository.findAll();
    }

    public TransportService getTransportServiceById(Long id) {
        return transportServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TransportService not found with id: " + id));
    }

    public TransportService createTransportService(TransportService service) {
        service.setId(null);
        return transportServiceRepository.save(service);
    }

    public TransportService updateTransportService(Long id, TransportService updated) {
        TransportService existing = getTransportServiceById(id);
        existing.setEvent(updated.getEvent());
        existing.setTransportType(updated.getTransportType());
        existing.setDeparturePlace(updated.getDeparturePlace());
        existing.setDepartureTime(updated.getDepartureTime());
        existing.setReturnTime(updated.getReturnTime());
        existing.setCapacity(updated.getCapacity());
        existing.setStatus(updated.getStatus());
        return transportServiceRepository.save(existing);
    }

    public void deleteTransportService(Long id) {
        if (!transportServiceRepository.existsById(id)) {
            throw new RuntimeException("TransportService not found with id: " + id);
        }
        transportServiceRepository.deleteById(id);
    }

    // ---- TransportBooking CRUD simplifié ----

    public List<TransportBooking> getAllBookings() {
        return transportBookingRepository.findAll();
    }

    public TransportBooking createBooking(TransportBooking booking) {
        booking.setId(null);
        return transportBookingRepository.save(booking);
    }

    public void deleteBooking(Long bookingId) {
        if (!transportBookingRepository.existsById(bookingId)) {
            throw new RuntimeException("TransportBooking not found with id: " + bookingId);
        }
        transportBookingRepository.deleteById(bookingId);
    }
}
