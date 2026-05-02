package tn.esprit.pi_back.services;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi_back.entities.InsuranceClaim;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import java.util.List;

public interface IInsuranceClaimService {
    InsuranceClaim submitClaim(Long voucherId, Long policyId, Long userId, String description, String claimReference, List<MultipartFile> files, Double amountRequested);
    InsuranceClaim updateStatus(Long claimId, ClaimStatus status, String comment, Double amountApproved, String rejectionReason, Long adminId);
    List<InsuranceClaim> getByClient(Long clientId);
    InsuranceClaim getById(Long id);
    List<InsuranceClaim> getAll();
    void delete(Long id);
}