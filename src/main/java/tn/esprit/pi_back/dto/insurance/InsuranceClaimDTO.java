package tn.esprit.pi_back.dto.insurance;

import java.time.LocalDateTime;

public record InsuranceClaimDTO(
        Long id,
        String claimReference,
        String status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime decidedAt,
        VoucherMiniDTO voucher,
        InsurancePolicyMiniDTO policy
) {}