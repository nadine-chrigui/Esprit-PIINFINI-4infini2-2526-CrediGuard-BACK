package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.insurance.InsuranceSimulationDTO;
import tn.esprit.pi_back.entities.RiskScore;

public interface IRiskScoreService {
    RiskScore calculate(Long userId, String sector, String region, Double value, String goodsNature);
    RiskScore simulate(String sector, String region, Double value, String goodsNature);
}
