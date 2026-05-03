package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.InsuranceClaim;

import java.util.List;

public interface IInsuranceClaimService {

    // create claim (voucher + policy)
    InsuranceClaim createClaim(Long idVoucher, Long idPolicy, String claimReference);

    // decision
    InsuranceClaim approve(Long idClaim);
    InsuranceClaim reject(Long idClaim, String reason);

    // CRUD classique
    InsuranceClaim update(InsuranceClaim claim);
    void delete(Long id);
    InsuranceClaim get(Long id);
    List<InsuranceClaim> all();
}