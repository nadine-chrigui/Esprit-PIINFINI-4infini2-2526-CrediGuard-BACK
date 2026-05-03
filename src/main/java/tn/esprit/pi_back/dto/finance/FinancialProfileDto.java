package tn.esprit.pi_back.dto.finance;

import lombok.Data;
import tn.esprit.pi_back.entities.FinancialProfile;

@Data
public class FinancialProfileDto {
    private Long id;
    private Long userId;
    private double score;
    private String profileType;
    private double savingsRate;
    private double repaymentRate;
    private double historyScore;
    private String updatedAt;

    public static FinancialProfileDto from(FinancialProfile p) {
        FinancialProfileDto dto = new FinancialProfileDto();
        dto.id = p.getId();
        dto.userId = p.getUser() != null ? p.getUser().getId() : null;
        dto.score = p.getScore();
        dto.profileType = p.getProfileType() != null ? p.getProfileType().name() : null;
        dto.savingsRate = p.getSavingsRate();
        dto.repaymentRate = p.getRepaymentRate();
        dto.historyScore = p.getHistoryScore();
        dto.updatedAt = p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null;
        return dto;
    }
}
