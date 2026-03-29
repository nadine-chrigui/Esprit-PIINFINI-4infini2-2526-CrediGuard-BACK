package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.TransportBookingRequestDto;
import tn.esprit.pi_back.dto.TransportBookingResponseDto;
import tn.esprit.pi_back.dto.TransportServiceRequestDto;
import tn.esprit.pi_back.dto.TransportServiceResponseDto;
import tn.esprit.pi_back.services.TransportManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/transports")
@CrossOrigin(origins = "*")
public class TransportController {

    private final TransportManagementService transportManagementService;

    public TransportController(TransportManagementService transportManagementService) {
        this.transportManagementService = transportManagementService;
    }

    // ===================== TransportService =====================

    // CREATE
    @PostMapping("/services")
    public ResponseEntity<TransportServiceResponseDto> createTransportService(@Valid @RequestBody TransportServiceRequestDto transportService) {
        return ResponseEntity.ok(transportManagementService.createTransportService(transportService));
    }

    // UPDATE
    @PutMapping("/services/{id}")
    public ResponseEntity<TransportServiceResponseDto> updateTransportService(@PathVariable Long id,
                                                                              @Valid @RequestBody TransportServiceRequestDto transportService) {
        return ResponseEntity.ok(transportManagementService.updateTransportService(id, transportService));
    }

    // GET BY ID
    @GetMapping("/services/{id}")
    public ResponseEntity<TransportServiceResponseDto> getTransportServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(transportManagementService.getTransportServiceById(id));
    }

    // GET ALL
    @GetMapping("/services")
    public ResponseEntity<List<TransportServiceResponseDto>> getAllTransportServices() {
        return ResponseEntity.ok(transportManagementService.getAllTransportServices());
    }

    // DELETE
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteTransportService(@PathVariable Long id) {
        transportManagementService.deleteTransportService(id);
        return ResponseEntity.noContent().build();
    }

    // ===================== TransportBooking =====================

    // CREATE
    @PostMapping("/bookings")
    public ResponseEntity<TransportBookingResponseDto> createBooking(@Valid @RequestBody TransportBookingRequestDto booking) {
        return ResponseEntity.ok(transportManagementService.createBooking(booking));
    }

    // UPDATE
    @PutMapping("/bookings/{id}")
    public ResponseEntity<TransportBookingResponseDto> updateBooking(@PathVariable Long id,
                                                                     @Valid @RequestBody TransportBookingRequestDto booking) {
        return ResponseEntity.ok(transportManagementService.updateBooking(id, booking));
    }

    // GET BY ID
    @GetMapping("/bookings/{id}")
    public ResponseEntity<TransportBookingResponseDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(transportManagementService.getBookingById(id));
    }

    // GET ALL
    @GetMapping("/bookings")
    public ResponseEntity<List<TransportBookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(transportManagementService.getAllBookings());
    }

    // DELETE
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        transportManagementService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
