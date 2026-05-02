package tn.esprit.pi_back.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class InsuranceRagService {

    @Value("${insurance-rag.url:http://127.0.0.1:8099/ask}")
    private String ragUrl;

    private final RestTemplate restTemplate;

    public InsuranceRagService() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(120000); // 120 seconds for AI generation
        this.restTemplate = new RestTemplate(factory);
    }

    public String askInsuranceAI(String question) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("query", question);

            log.info("--- APPEL RAG IA ---");
            log.info("URL : {}", ragUrl);
            log.info("Question envoyée : {}", question);
            
            Map<String, Object> response = restTemplate.postForObject(ragUrl, request, Map.class);

            if (response != null) {
                log.info("Réponse reçue du service Python : {}", response);
                if (response.containsKey("answer") && response.get("answer") != null) {
                    return (String) response.get("answer");
                } else if (response.containsKey("error")) {
                    log.error("L'IA a renvoyé une erreur : {}", response.get("error"));
                }
            } else {
                log.warn("Le service Python a renvoyé une réponse vide (null)");
            }

        } catch (Exception e) {
            log.error("ERREUR CRITIQUE lors de l'appel à l'IA : {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }
}
