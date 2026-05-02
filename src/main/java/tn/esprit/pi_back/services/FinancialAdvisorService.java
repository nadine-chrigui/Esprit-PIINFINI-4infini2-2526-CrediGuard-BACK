package tn.esprit.pi_back.services;

import java.util.Map;

public interface FinancialAdvisorService {
    Map<String, Object> getAdvisorReport(Long demandeId);
}
