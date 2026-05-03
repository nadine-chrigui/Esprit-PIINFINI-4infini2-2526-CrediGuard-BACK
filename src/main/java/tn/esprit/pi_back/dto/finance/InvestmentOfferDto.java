package tn.esprit.pi_back.dto.finance;

import lombok.Data;
import tn.esprit.pi_back.entities.InvestmentOffer;

@Data
public class InvestmentOfferDto {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String riskLevel;
    private double estimatedReturn;
    private String status;

    public static InvestmentOfferDto from(InvestmentOffer offer) {
        InvestmentOfferDto dto = new InvestmentOfferDto();
        dto.id = offer.getId();
        dto.title = offer.getTitle();
        dto.description = offer.getDescription();
        dto.type = offer.getType() != null ? offer.getType().name() : null;
        dto.riskLevel = offer.getRiskLevel();
        dto.estimatedReturn = offer.getEstimatedReturn();
        dto.status = offer.getStatus() != null ? offer.getStatus().name() : null;
        return dto;
    }
}
