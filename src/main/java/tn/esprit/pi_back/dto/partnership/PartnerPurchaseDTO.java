package tn.esprit.pi_back.dto.partnership;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPurchaseDTO {
    private Long id;
    private String purchaseReference;
    private Double totalAmount;
    private String productNames;
    private LocalDateTime createdAt;
    private Long clientId;
    private String clientName;
    private Long partnerId;
    private String partnerName;
    private String voucherCode;
}
