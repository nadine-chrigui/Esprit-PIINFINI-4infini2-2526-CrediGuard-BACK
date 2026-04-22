package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.InsuranceClaim;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.entities.Voucher;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.repositories.InsuranceClaimRepository;
import tn.esprit.pi_back.repositories.InsurancePolicyRepository;
import tn.esprit.pi_back.repositories.VoucherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceClaimServiceImpl implements IInsuranceClaimService {

    private final InsuranceClaimRepository claimRepo;
    private final VoucherRepository voucherRepo;
    private final InsurancePolicyRepository policyRepo;

    @Override
    public InsuranceClaim createClaim(Long idVoucher, Long idPolicy, String claimReference) {

        Voucher v = voucherRepo.findById(idVoucher)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found: " + idVoucher));

        InsurancePolicy p = policyRepo.findById(idPolicy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy not found: " + idPolicy));

        if (v.getInsuranceClaim() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher already has a claim");
        }

        InsuranceClaim c = new InsuranceClaim();
        c.setClaimReference(claimReference);
        c.setVoucher(v);
        c.setInsurancePolicy(p);
        c.setStatus(ClaimStatus.PENDING);

        // 🔥 SCORING (fix BigDecimal)
        int riskScore = new java.util.Random().nextInt(40);

        if (v.getAmount().doubleValue() > 500) riskScore += 40;
        if (v.getAmount().doubleValue() > 1000) riskScore += 20;

        c.setRiskScore(riskScore);

        // 🔥 ANALYSE
        if (riskScore < 20) {
            c.setAnalysis("SAFE");
        } else if (riskScore < 60) {
            c.setAnalysis("SUSPECT");
        } else {
            c.setAnalysis("HIGH RISK");
        }

        return claimRepo.save(c);
    }

    @Override
    public InsuranceClaim approve(Long idClaim) {
        InsuranceClaim c = claimRepo.findById(idClaim).orElse(null);
        if (c == null) return null;

        c.setStatus(ClaimStatus.APPROVED);
        c.setReason(null);
        c.setDecidedAt(LocalDateTime.now());
        return claimRepo.save(c);
    }

    @Override
    public InsuranceClaim reject(Long idClaim, String reason) {
        InsuranceClaim c = claimRepo.findById(idClaim).orElse(null);
        if (c == null) return null;

        c.setStatus(ClaimStatus.REJECTED);
        c.setReason(reason);
        c.setDecidedAt(LocalDateTime.now());
        return claimRepo.save(c);
    }

    // CRUD
    @Override
    public InsuranceClaim update(InsuranceClaim claim) {
        return claimRepo.save(claim);
    }

    @Override
    public void delete(Long id) {
        claimRepo.deleteById(id);
    }

    @Override
    public InsuranceClaim get(Long id) {
        return claimRepo.findById(id).orElse(null);
    }

    @Override
    public List<InsuranceClaim> all() {
        return claimRepo.findAll();
    }
}