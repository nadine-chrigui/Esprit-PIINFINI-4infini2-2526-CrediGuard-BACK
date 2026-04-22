package tn.esprit.pi_back.dto.insurance;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatbotResponseDTO {
    private String response;
    private String intent;
    private String status;
    private String suggestedAction;

    public ChatbotResponseDTO(String response, String intent, String status, String suggestedAction) {
        this.response = response;
        this.intent = intent;
        this.status = status;
        this.suggestedAction = suggestedAction;
    }
}
