package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.FinancialProfile;

public interface ScoringService {
    FinancialProfile calculateAndSaveProfile(Long userId);
    FinancialProfile getProfile(Long userId);
}