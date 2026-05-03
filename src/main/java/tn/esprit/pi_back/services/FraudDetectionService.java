package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pi_back.dto.fraud.FraudRequestDto;
import tn.esprit.pi_back.dto.fraud.FraudResponseDto;
import tn.esprit.pi_back.entities.FraudAnalysisResult;
import tn.esprit.pi_back.repositories.FraudAnalysisRepository;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final FraudAnalysisRepository fraudRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${crediguard.fraud.api.url:http://localhost:5001}")
    private String fraudApiUrl;

    @Async
    public void analyserAsync(Long txId, Long carteId, Long userId, FraudRequestDto requestDto) {
        log.info("Lancement de l'analyse de fraude pour la transaction {}", txId);
        
        try {
            String url = fraudApiUrl + "/api/fraud/predict";
            FraudResponseDto response = restTemplate.postForObject(url, requestDto, FraudResponseDto.class);

            if (response != null) {
                FraudAnalysisResult result = FraudAnalysisResult.builder()
                        .transactionId(txId)
                        .carteId(carteId)
                        .userId(userId)
                        .scoreFraude(response.getScoreFraude())
                        .probabiliteRF(response.getProbabiliteRF())
                        .decision(response.getDecision())
                        .niveau(response.getNiveau())
                        .raisons(response.getRaisons())
                        .build();
                
                fraudRepository.save(result);
                log.info("Analyse de fraude terminée pour {}: Décision={}", txId, response.getDecision());
                
                if ("BLOQUEE".equals(response.getDecision())) {
                    log.warn("ALERTE CRITIQUE: Transaction {} bloquée pour suspicion de fraude !", txId);
                    // Ici on pourrait appeler un service de notification ou bloquer la carte
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API de détection de fraude: {}", e.getMessage());
            
            // Fallback: enregistrer une erreur technique
            FraudAnalysisResult technicalError = FraudAnalysisResult.builder()
                    .transactionId(txId)
                    .carteId(carteId)
                    .userId(userId)
                    .decision("ERREUR")
                    .niveau("INCONNU")
                    .raisons(Collections.singletonList("API ML indisponible: " + e.getMessage()))
                    .build();
            fraudRepository.save(technicalError);
        }
    }

    public FraudAnalysisResult getResultForTransaction(Long txId) {
        return fraudRepository.findByTransactionId(txId).orElse(null);
    }
}
