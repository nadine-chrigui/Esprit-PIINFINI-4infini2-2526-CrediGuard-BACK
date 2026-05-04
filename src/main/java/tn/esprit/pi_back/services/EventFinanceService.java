package tn.esprit.pi_back.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.repositories.EventRepository;
import tn.esprit.pi_back.repositories.EventRegistrationRepository;
import tn.esprit.pi_back.dtos.EventProfitability;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EventFinanceService {

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private EventRegistrationRepository registrationRepo;

    public EventProfitability calculateProfitability(Long eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé avec l'ID: " + eventId));

        // Revenus
        BigDecimal ticketRevenue = calculateTicketRevenue(eventId);
        BigDecimal sponsorshipRevenue = calculateSponsorshipRevenue(eventId);
        BigDecimal totalRevenue = ticketRevenue.add(sponsorshipRevenue);

        // Coûts
        BigDecimal venueCost = calculateVenueCost(eventId);
        BigDecimal marketingCost = calculateMarketingCost(eventId);
        BigDecimal staffCost = calculateStaffCost(eventId);
        BigDecimal equipmentCost = calculateEquipmentCost(eventId);
        BigDecimal totalCosts = venueCost.add(marketingCost).add(staffCost).add(equipmentCost);

        // Calculs
        BigDecimal profit = totalRevenue.subtract(totalCosts);
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
            profit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
            BigDecimal.ZERO;

        Integer breakEvenAttendees = calculateBreakEven(eventId);
        Long currentRegistrations = registrationRepo.countByEventId(eventId);

        return EventProfitability.builder()
            .eventId(eventId)
            .eventName(event.getTitle())
            .totalRevenue(totalRevenue)
            .totalCosts(totalCosts)
            .profit(profit)
            .profitMargin(profitMargin)
            .breakEvenAttendees(breakEvenAttendees)
            .currentRegistrations(currentRegistrations)
            .isProfitable(profit.compareTo(BigDecimal.ZERO) > 0)
            .calculationDate(LocalDate.now())
            .build();
    }

    private BigDecimal calculateTicketRevenue(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        Long registrations = registrationRepo.countByEventId(eventId);
        
        // Utilise le prix réel du ticket depuis l'Event
        return event.getTicketPrice().multiply(BigDecimal.valueOf(registrations));
    }

    private BigDecimal calculateSponsorshipRevenue(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        // Utilise le montant de sponsoring réel depuis l'Event
        return event.getSponsorshipAmount();
    }

    private BigDecimal calculateVenueCost(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        // Utilise le coût réel de la salle depuis l'Event
        return event.getVenueCost();
    }

    private BigDecimal calculateMarketingCost(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        // Utilise le coût marketing réel depuis l'Event
        return event.getMarketingCost();
    }

    private BigDecimal calculateStaffCost(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        // Utilise le coût staff réel depuis l'Event
        return event.getStaffCost();
    }

    private BigDecimal calculateEquipmentCost(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        // Utilise le coût équipement réel depuis l'Event
        return event.getEquipmentCost();
    }

    private Integer calculateBreakEven(Long eventId) {
        Event event = eventRepo.findById(eventId).get();
        BigDecimal totalCosts = calculateVenueCost(eventId)
                .add(calculateMarketingCost(eventId))
                .add(calculateStaffCost(eventId))
                .add(event.getEquipmentCost());  // Ajout du coût équipement
        
        // Utilise le prix réel du ticket depuis l'Event
        return totalCosts.divide(event.getTicketPrice(), 0, RoundingMode.UP).intValue();
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }
}
