package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.TransportService;
import tn.esprit.pi_back.repositories.TransportServiceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportServiceService {

    private final TransportServiceRepository transportServiceRepository;

    public List<TransportService> getAll() {
        return transportServiceRepository.findAll();
    }

    public List<TransportService> getByEventId(Long eventId) {
        return transportServiceRepository.findByEventId(eventId);
    }

    public TransportService getById(Long id) {
        return transportServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transport service not found"));
    }

    public TransportService create(TransportService transportService) {
        transportService.setId(null);
        return transportServiceRepository.save(transportService);
    }

    public TransportService update(Long id, TransportService updated) {
        TransportService existing = getById(id);

        existing.setCapacity(updated.getCapacity());
        existing.setDeparturePlace(updated.getDeparturePlace());
        existing.setDepartureTime(updated.getDepartureTime());
        existing.setReturnTime(updated.getReturnTime());
        existing.setStatus(updated.getStatus());
        existing.setTransportType(updated.getTransportType());
        existing.setEventId(updated.getEventId());
        existing.setCreatedByUserId(updated.getCreatedByUserId());

        return transportServiceRepository.save(existing);
    }

    public void delete(Long id) {
        transportServiceRepository.deleteById(id);
    }
}
