package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.repositories.EventRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event create(Event event) {
        System.out.println("=== DEBUG EventService.create() ===");
        System.out.println("Event reçu: " + event);
        System.out.println("Titre: " + event.getTitle());
        System.out.println("Prix ticket: " + event.getTicketPrice());
        System.out.println("Coût salle: " + event.getVenueCost());
        
        event.setId(null);
        
        // Assurer que les champs financiers ont des valeurs par défaut
        if (event.getTicketPrice() == null) {
            event.setTicketPrice(BigDecimal.valueOf(50.0));
            System.out.println("Prix ticket mis à défaut: 50.0");
        }
        if (event.getSponsorshipAmount() == null) {
            event.setSponsorshipAmount(BigDecimal.ZERO);
        }
        if (event.getVenueCost() == null) {
            event.setVenueCost(BigDecimal.ZERO);
        }
        if (event.getMarketingCost() == null) {
            event.setMarketingCost(BigDecimal.ZERO);
        }
        if (event.getStaffCost() == null) {
            event.setStaffCost(BigDecimal.ZERO);
        }
        if (event.getEquipmentCost() == null) {
            event.setEquipmentCost(BigDecimal.ZERO);
        }
        if (event.getBudgetEstimated() == null) {
            event.setBudgetEstimated(BigDecimal.ZERO);
        }
        
        Event saved = eventRepository.save(event);
        System.out.println("Event sauvegardé avec ID: " + saved.getId());
        return saved;
    }

    public Event update(Long id, Event updated) {
        Event existing = getById(id);

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setCapacity(updated.getCapacity());
        existing.setDateStart(updated.getDateStart());
        existing.setDateEnd(updated.getDateEnd());
        existing.setEventType(updated.getEventType());
        existing.setStatus(updated.getStatus());
        existing.setCreatedByUserId(updated.getCreatedByUserId());
        
        // Mise à jour des champs financiers
        if (updated.getTicketPrice() != null) {
            existing.setTicketPrice(updated.getTicketPrice());
        }
        if (updated.getSponsorshipAmount() != null) {
            existing.setSponsorshipAmount(updated.getSponsorshipAmount());
        }
        if (updated.getVenueCost() != null) {
            existing.setVenueCost(updated.getVenueCost());
        }
        if (updated.getMarketingCost() != null) {
            existing.setMarketingCost(updated.getMarketingCost());
        }
        if (updated.getStaffCost() != null) {
            existing.setStaffCost(updated.getStaffCost());
        }
        if (updated.getEquipmentCost() != null) {
            existing.setEquipmentCost(updated.getEquipmentCost());
        }
        if (updated.getBudgetEstimated() != null) {
            existing.setBudgetEstimated(updated.getBudgetEstimated());
        }

        return eventRepository.save(existing);
    }

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }
}
