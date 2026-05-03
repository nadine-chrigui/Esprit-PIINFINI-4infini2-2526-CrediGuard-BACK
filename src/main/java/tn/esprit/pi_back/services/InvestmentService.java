package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InvestmentOffer;
import tn.esprit.pi_back.entities.PerformanceTracking;
import java.util.List;

public interface InvestmentService {
    List<InvestmentOffer> getOffersForUser(Long userId);
    InvestmentOffer acceptOffer(Long offerId);
    List<PerformanceTracking> getPerformance(Long offerId);
    void updatePerformance(Long offerId, double newValue);
}
