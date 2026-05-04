package tn.esprit.pi_back.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.services.EventPredictionService;

import java.util.Map;

@RestController
@RequestMapping("/events/{eventId}/prediction")
@CrossOrigin(origins = "http://localhost:4200")
public class EventPredictionController {

    @Autowired
    private EventPredictionService predictionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> predictEventSuccess(@PathVariable Long eventId) {
        try {
            Map<String, Object> prediction = predictionService.predictEventSuccess(eventId);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> predictAllEvents() {
        try {
            Map<String, Object> predictions = predictionService.predictAllEvents();
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
