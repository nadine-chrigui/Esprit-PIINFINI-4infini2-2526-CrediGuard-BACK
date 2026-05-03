package tn.esprit.pi_back.dto.insurance;

import lombok.Data;

@Data
public class ClaimRequest {

    private Long voucherId;
    private Long policyId;
    private String claimReference;

}