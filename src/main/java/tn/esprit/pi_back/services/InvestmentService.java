package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InvestmentOffer;
import tn.esprit.pi_back.entities.PerformanceTracking;
import tn.esprit.pi_back.dto.Investment.*;
import java.util.List;

public interface InvestmentService {
    InvestmentResponse create(InvestmentCreateRequest req);
    List<InvestmentResponse> getAll();
    InvestmentResponse getById(Long id);
    List<InvestmentResponse> getByInvestor(Long investorId);
    List<InvestmentResponse> getByProject(Long projectId);
    InvestmentResponse update(Long id, InvestmentUpdateRequest req);
    void delete(Long id);
    List<InvestmentOffer> getOffersForUser(Long userId);
    InvestmentOffer acceptOffer(Long offerId);
    List<PerformanceTracking> getPerformance(Long offerId);
    void updatePerformance(Long offerId, double newValue);
}