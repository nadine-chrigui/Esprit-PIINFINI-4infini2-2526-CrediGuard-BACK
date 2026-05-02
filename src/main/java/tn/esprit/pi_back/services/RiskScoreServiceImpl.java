package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.RiskScore;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.RiskScoreRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RiskScoreServiceImpl implements IRiskScoreService {

    private final RiskScoreRepository riskScoreRepository;
    private final UserRepository userRepository;

    @Override
    public RiskScore calculate(Long userId, String sector, String region, Double value, String goodsNature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        RiskScore score = computeLogic(sector, region, value, goodsNature);
        score.setUser(user);
        
        // Save or Update
        RiskScore existing = riskScoreRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            existing.setGlobalScore(score.getGlobalScore());
            existing.setSectorScore(score.getSectorScore());
            existing.setRegionScore(score.getRegionScore());
            existing.setHistoryScore(score.getHistoryScore());
            existing.setFactorDetails(score.getFactorDetails());
            existing.setComputedAt(LocalDateTime.now());
            return riskScoreRepository.save(existing);
        }
        
        return riskScoreRepository.save(score);
    }

    @Override
    public RiskScore simulate(String sector, String region, Double value, String goodsNature) {
        return computeLogic(sector, region, value, goodsNature);
    }

    private RiskScore computeLogic(String sector, String region, Double value, String goodsNature) {
        RiskScore rs = new RiskScore();
        Random r = new Random();

        // Mock external API calls
        int sectorScore = r.nextInt(101); // 0-100
        int regionScore = r.nextInt(101);
        int historyScore = r.nextInt(101);

        // Simple aggregation
        int globalScore = (sectorScore + regionScore + historyScore) / 3;

        rs.setSectorScore(sectorScore);
        rs.setRegionScore(regionScore);
        rs.setHistoryScore(historyScore);
        rs.setGlobalScore(globalScore);
        rs.setFactorDetails("{\"weather\": \"Stable\", \"economy\": \"Growing\", \"claims_history\": \"Normal\"}");
        rs.setComputedAt(LocalDateTime.now());

        return rs;
    }
}
