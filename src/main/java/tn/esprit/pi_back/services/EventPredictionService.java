package tn.esprit.pi_back.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Event;
import tn.esprit.pi_back.repositories.EventRepository;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EventPredictionService {

    @Autowired
    private EventRepository eventRepository;

    public Map<String, Object> predictEventSuccess(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        // Calculer les features
        Map<String, Object> features = calculateFeatures(event);

        // Score simple basé sur des règles (remplace le ML complexe)
        double successScore = calculateSuccessScore(features);

        Map<String, Object> result = new HashMap<>();
        result.put("eventId", eventId);
        result.put("eventTitle", event.getTitle());
        result.put("successScore", Math.round(successScore * 100));
        result.put("prediction", successScore >= 0.6 ? "SUCCESS" : "RISK");
        result.put("confidence", calculateConfidence(successScore));
        result.put("features", features);
        result.put("recommendations", generateRecommendations(features, successScore));

        return result;
    }

    private Map<String, Object> calculateFeatures(Event event) {
        Map<String, Object> features = new HashMap<>();

        // Récupération sécurisée des valeurs de l'entité avec conversion BigDecimal -> Double
        Integer capacity = event.getCapacity() != null ? event.getCapacity() : 0;
        Double budgetEstimated = event.getBudgetEstimated() != null ? event.getBudgetEstimated().doubleValue() : 0.0;
        Double ticketPrice = event.getTicketPrice() != null ? event.getTicketPrice().doubleValue() : 0.0;
        Double marketingCost = event.getMarketingCost() != null ? event.getMarketingCost().doubleValue() : 0.0;
        
        // Features financières calculées en temps réel
        double budgetPerPerson = (capacity > 0 && budgetEstimated > 0) ? budgetEstimated / capacity : 0.0;
        features.put("budgetPerPerson", Math.round(budgetPerPerson * 100.0) / 100.0);

        double potentialRevenue = ticketPrice * capacity;
        double profitMargin = (budgetEstimated > 0) ? 
                ((potentialRevenue - budgetEstimated) / budgetEstimated) * 100 : 0.0;
        features.put("profitMargin", Math.round(profitMargin * 100.0) / 100.0);

        // Features temporelles
        long duration;
        if (event.getDateStart() != null && event.getDateEnd() != null) {
            duration = ChronoUnit.DAYS.between(event.getDateStart(), event.getDateEnd());
        } else {
            duration = 1L;
        }
        features.put("duration", duration);
        
        String season;
        if (event.getDateStart() != null) {
            season = getSeason(event.getDateStart());
        } else {
            season = "printemps";
        }
        features.put("season", season);

        // Features de coûts calculées en temps réel
        double marketingRatio = (budgetEstimated > 0) ? (marketingCost / budgetEstimated) * 100 : 0.0;
        features.put("marketingRatio", Math.round(marketingRatio * 100.0) / 100.0);

        // Features de capacité
        features.put("capacity", capacity);
        features.put("ticketPrice", ticketPrice);

        return features;
    }

    private double calculateSuccessScore(Map<String, Object> features) {
        double score = 0.0;

        // Budget par personne (0-25 points)
        double budgetPerPerson = (Double) features.get("budgetPerPerson");
        if (budgetPerPerson <= 100) score += 25;
        else if (budgetPerPerson <= 200) score += 20;
        else if (budgetPerPerson <= 300) score += 15;
        else score += 5;

        // Marge de profit (0-25 points)
        double profitMargin = (Double) features.get("profitMargin");
        if (profitMargin >= 50) score += 25;
        else if (profitMargin >= 20) score += 20;
        else if (profitMargin >= 0) score += 15;
        else score += 5;

        // Saison (0-15 points)
        String season = (String) features.get("season");
        if ("printemps".equals(season) || "été".equals(season)) score += 15;
        else if ("automne".equals(season)) score += 10;
        else score += 5;

        // Ratio marketing (0-20 points)
        double marketingRatio = (Double) features.get("marketingRatio");
        if (marketingRatio >= 15 && marketingRatio <= 25) score += 20;
        else if (marketingRatio >= 10 && marketingRatio <= 30) score += 15;
        else score += 5;

        // Durée (0-15 points)
        long duration = (Long) features.get("duration");
        if (duration >= 1 && duration <= 3) score += 15;
        else if (duration <= 7) score += 10;
        else score += 5;

        return score / 100.0; // Normaliser entre 0 et 1
    }

    private String calculateConfidence(double score) {
        if (score >= 0.8 || score <= 0.2) return "ÉLEVÉE";
        else if (score >= 0.6 || score <= 0.4) return "MOYENNE";
        else return "FAIBLE";
    }

    private List<String> generateRecommendations(Map<String, Object> features, double score) {
        List<String> recommendations = new ArrayList<>();

        // Récupération sécurisée des valeurs
        Double budgetPerPersonObj = (Double) features.get("budgetPerPerson");
        Double profitMarginObj = (Double) features.get("profitMargin");
        Double marketingRatioObj = (Double) features.get("marketingRatio");
        String season = (String) features.get("season");
        Long durationObj = (Long) features.get("duration");

        double budgetPerPerson = budgetPerPersonObj != null ? budgetPerPersonObj : 0.0;
        double profitMargin = profitMarginObj != null ? profitMarginObj : 0.0;
        double marketingRatio = marketingRatioObj != null ? marketingRatioObj : 0.0;
        long duration = durationObj != null ? durationObj : 1L;

        if (budgetPerPerson > 300) {
            recommendations.add("💰 Reduce budget per person - it seems too high");
        }

        if (profitMargin < 0) {
            recommendations.add("📉 Increase ticket price or reduce costs to ensure profitability");
        }

        if (marketingRatio < 10) {
            recommendations.add("📢 Increase marketing budget (10-25% of total budget)");
        }

        if ("winter".equals(season)) {
            recommendations.add("🌤️ Consider moving the event to spring/summer for better attendance");
        }

        if (duration > 7) {
            recommendations.add("⏰ Reduce duration - long events have less success");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ The event seems well configured to succeed!");
        }

        return recommendations;
    }

    private String getSeason(java.time.LocalDateTime date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) return "spring";
        else if (month >= 6 && month <= 8) return "summer";
        else if (month >= 9 && month <= 11) return "autumn";
        else return "winter";
    }

    public Map<String, Object> predictAllEvents() {
        List<Event> events = eventRepository.findAll();
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> predictions = new ArrayList<>();
        int successCount = 0;
        int totalEvents = events.size();

        for (Event event : events) {
            try {
                Map<String, Object> prediction = predictEventSuccess(event.getId());
                predictions.add(prediction);

                if ("SUCCESS".equals(prediction.get("prediction"))) {
                    successCount++;
                }
            } catch (Exception e) {
                // Ignorer les erreurs individuelles
            }
        }

        result.put("predictions", predictions);
        result.put("totalEvents", totalEvents);
        result.put("successCount", successCount);
        double successRate = totalEvents > 0 ? (successCount * 100.0 / totalEvents) : 0.0;
        result.put("successRate", successRate);

        return result;
    }
}
