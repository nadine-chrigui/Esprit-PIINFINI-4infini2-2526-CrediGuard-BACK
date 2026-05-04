package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimMapper;
import tn.esprit.pi_back.dto.insurance.CreateClaimRequest;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.repositories.InsurancePolicyRepository;
import tn.esprit.pi_back.services.IInsuranceClaimService;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance/claims")
@CrossOrigin("*")
@Transactional
public class InsuranceClaimRestController {

    private final IInsuranceClaimService service;
    private final InsurancePolicyRepository policyRepository;
    private final tn.esprit.pi_back.services.UserService userService;

    @PostMapping("/create")
    public InsuranceClaimDTO create(@RequestBody CreateClaimRequest request) {
        try {
            // Resolve the real client ID from the policy so the claim is attributed correctly
            InsurancePolicy policy = policyRepository.findById(request.getPolicyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy not found: " + request.getPolicyId()));

            Long userId;
            if (policy.getClient() != null) {
                userId = policy.getClient().getId();
            } else {
                // Fallback to the current authenticated user instead of hardcoded 1L
                userId = userService.getOrCreateCurrentUser().getId();
            }

            return InsuranceClaimMapper.toDTO(
                    service.submitClaim(
                            request.getVoucherId(),
                            request.getPolicyId(),
                            userId,
                            "Achat Partenaire",
                            request.getClaimReference(),
                            null,
                            0.0
                    )
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création du sinistre : " + e.getMessage());
        }
    }

    @GetMapping("/by-client/{clientId}")
    public List<InsuranceClaimDTO> getByClient(@PathVariable Long clientId) {
        return service.getByClient(clientId)
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
    }

    @PutMapping("/approve/{idClaim}")
    public InsuranceClaimDTO approve(@PathVariable Long idClaim) {
        return InsuranceClaimMapper.toDTO(
                service.updateStatus(idClaim, ClaimStatus.APPROVED, "Approved via Legacy API", 0.0, null, 1L)
        );
    }

    @PutMapping("/reject/{idClaim}")
    public InsuranceClaimDTO reject(@PathVariable Long idClaim,
                                    @RequestParam String reason) {
        return InsuranceClaimMapper.toDTO(
                service.updateStatus(idClaim, ClaimStatus.REJECTED, "Rejected via Legacy API", 0.0, reason, 1L)
        );
    }

    @PutMapping("/update")
    public InsuranceClaimDTO update(@RequestBody InsuranceClaimDTO dto) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Use approve/reject instead");
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/get/{id}")
    public InsuranceClaimDTO get(@PathVariable Long id) {
        var c = service.getById(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found");
        return InsuranceClaimMapper.toDTO(c);
    }

    @GetMapping("/all")
    public List<InsuranceClaimDTO> all() {
        return service.getAll()
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
    }
}