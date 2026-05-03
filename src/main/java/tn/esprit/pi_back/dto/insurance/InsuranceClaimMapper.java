package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.InsuranceClaim;
import tn.esprit.pi_back.entities.Voucher;

public class InsuranceClaimMapper {

    public static InsuranceClaimDTO toDTO(InsuranceClaim c) {

        Voucher v = c.getVoucher();

        return new InsuranceClaimDTO(
                c.getId(),
                c.getClaimReference(),
                c.getStatus().name(),
                c.getReason(),
                c.getCreatedAt(),
                c.getDecidedAt(),

                new VoucherMiniDTO(
                        v.getId(),
                        v.getCode(),
                        v.getAmount(),
                        v.getStatus().name(),
                        v.getExpirationDate(),
                        UserMapper.toClientDTO(v.getClient()) // 🔥 FIX PRO
                ),

                new InsurancePolicyMiniDTO(
                        c.getInsurancePolicy().getId(),
                        c.getInsurancePolicy().getPolicyNumber()
                )
        );
    }
}