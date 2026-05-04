package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.TransportService;
import tn.esprit.pi_back.services.TransportServiceService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/transports/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransportServiceController {

    private final TransportServiceService transportServiceService;

    @GetMapping
    public ResponseEntity<List<TransportService>> getAll(@RequestParam(value = "eventId", required = false) Long eventId) {
        if (eventId != null) {
            return ResponseEntity.ok(transportServiceService.getByEventId(eventId));
        }
        return ResponseEntity.ok(transportServiceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportService> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transportServiceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TransportService> create(@Valid @RequestBody TransportService transportService) {
        TransportService created = transportServiceService.create(transportService);
        return ResponseEntity.created(URI.create("/api/transport-services/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportService> update(@PathVariable Long id, @Valid @RequestBody TransportService transportService) {
        return ResponseEntity.ok(transportServiceService.update(id, transportService));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transportServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
