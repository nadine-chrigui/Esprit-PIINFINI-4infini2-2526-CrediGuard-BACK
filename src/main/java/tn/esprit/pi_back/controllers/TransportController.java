package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.TransportBooking;
import tn.esprit.pi_back.entities.TransportService;
import tn.esprit.pi_back.services.TransportServiceManager;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportServiceManager transportServiceManager;

    // ---- TransportService CRUD ----

    @GetMapping("/services")
    public List<TransportService> getAllTransportServices() {
        return transportServiceManager.getAllTransportServices();
    }

    @GetMapping("/services/{id}")
    public TransportService getTransportService(@PathVariable Long id) {
        return transportServiceManager.getTransportServiceById(id);
    }

    @PostMapping("/services")
    public TransportService createTransportService(@RequestBody TransportService service) {
        return transportServiceManager.createTransportService(service);
    }

    @PutMapping("/services/{id}")
    public TransportService updateTransportService(@PathVariable Long id,
                                                   @RequestBody TransportService service) {
        return transportServiceManager.updateTransportService(id, service);
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteTransportService(@PathVariable Long id) {
        transportServiceManager.deleteTransportService(id);
        return ResponseEntity.noContent().build();
    }

    // ---- TransportBooking ----

    @GetMapping("/services/{serviceId}/bookings")
    public List<TransportBooking> getBookingsByService(@PathVariable Long serviceId) {
        return transportServiceManager.getBookingsByTransportService(serviceId);
    }

    @GetMapping("/beneficiaries/{beneficiaryId}/bookings")
    public List<TransportBooking> getBookingsByBeneficiary(@PathVariable Long beneficiaryId) {
        return transportServiceManager.getBookingsByBeneficiary(beneficiaryId);
    }

    @PostMapping("/bookings")
    public TransportBooking createBooking(@RequestBody TransportBooking booking) {
        return transportServiceManager.createBooking(booking);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        transportServiceManager.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
