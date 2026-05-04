package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.InsuranceClaim;

public class InsuranceClaimMapper {

    public static InsuranceClaimDTO toDTO(InsuranceClaim c) {
        if (c == null) return null;

        InsurancePolicyMiniDTO policyDTO = null;
        if (c.getInsurancePolicy() != null) {
            policyDTO = new InsurancePolicyMiniDTO(
                    c.getInsurancePolicy().getId(),
                    c.getInsurancePolicy().getPolicyNumber()
            );
        }

        VoucherMiniDTO voucherDTO = null;
        if (c.getVoucher() != null) {
            ClientDTO clientDTO = null;
            if (c.getVoucher().getClient() != null) {
                clientDTO = new ClientDTO(
                        c.getVoucher().getClient().getId(),
                        c.getVoucher().getClient().getFullName(),
                        c.getVoucher().getClient().getEmail()
                );
            }
            voucherDTO = new VoucherMiniDTO(
                    c.getVoucher().getId(),
                    c.getVoucher().getCode(),
                    c.getVoucher().getAmount(),
                    c.getVoucher().getStatus() != null ? c.getVoucher().getStatus().name() : null,
                    c.getVoucher().getExpirationDate(),
                    clientDTO
            );
        }

        return new InsuranceClaimDTO(
                c.getId(),
                c.getClaimNumber(),
                c.getStatus() != null ? c.getStatus().name() : "PENDING",
                c.getRejectionReason(),
                c.getDeclaredAt(),
                c.getDecidedAt(),
                c.getAmountRequested(),
                c.getFraudScore(),
                c.getRiskScore(),
                voucherDTO,
                policyDTO
        );
    }
}