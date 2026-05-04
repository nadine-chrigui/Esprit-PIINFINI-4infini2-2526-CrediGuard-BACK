package tn.esprit.pi_back.dto.insurance;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatbotRequestDTO {
    private String question;
    private Long clientId;

    public ChatbotRequestDTO(String question) {
        this.question = question;
    }

    public ChatbotRequestDTO(String question, Long clientId) {
        this.question = question;
        this.clientId = clientId;
    }
}
