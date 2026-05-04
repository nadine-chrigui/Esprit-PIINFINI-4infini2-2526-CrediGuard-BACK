package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Ml.CrowdfundingSuccessPredictionResponse;

public interface CrowdfundingSuccessPredictionService {
    CrowdfundingSuccessPredictionResponse refreshPrediction(Long projectId);
    CrowdfundingSuccessPredictionResponse getLatestPrediction(Long projectId);
}
