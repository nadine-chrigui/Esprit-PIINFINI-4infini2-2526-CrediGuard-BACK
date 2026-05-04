package tn.esprit.pi_back.dto.partnership;

import lombok.Data;

@Data
public class CreatePurchaseRequest {
    private Long clientId;
    private Long partnerId;
    private Long voucherId;
    private Double totalAmount;
    private String productNames;
}
