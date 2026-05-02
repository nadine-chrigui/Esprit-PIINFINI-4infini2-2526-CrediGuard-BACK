package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimMapper;
import tn.esprit.pi_back.dto.insurance.SegmentationRequestDTO;
import tn.esprit.pi_back.dto.insurance.SegmentationResponseDTO;
import tn.esprit.pi_back.entities.InsuranceClaim;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.services.ClientSegmentationService;
import tn.esprit.pi_back.services.IInsuranceClaimService;

import java.util.List;

@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ClaimController {

    private final IInsuranceClaimService claimService;
    private final ClientSegmentationService segmentationService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<InsuranceClaimDTO> submit(@RequestParam Long policyId,
                                               @RequestParam Long userId,
                                               @RequestParam String description,
                                               @RequestParam Double amountRequested,
                                               @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        InsuranceClaim claim = claimService.submitClaim(null, policyId, userId, description, "MANUAL-" + System.currentTimeMillis(), files, amountRequested);
        return ResponseEntity.ok(InsuranceClaimMapper.toDTO(claim));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<InsuranceClaimDTO> updateStatus(@PathVariable Long id,
                                                      @RequestParam ClaimStatus status,
                                                      @RequestParam(required = false) String comment,
                                                      @RequestParam(required = false) Double amountApproved,
                                                      @RequestParam(required = false) String rejectionReason,
                                                      @RequestParam(required = false, defaultValue = "1") Long adminId) {
        InsuranceClaim claim = claimService.updateStatus(id, status, comment, amountApproved, rejectionReason, adminId);
        return ResponseEntity.ok(InsuranceClaimMapper.toDTO(claim));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InsuranceClaimDTO>> getByClient(@PathVariable Long clientId) {
        List<InsuranceClaimDTO> dtos = claimService.getByClient(clientId)
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Auto-segment a claim using its linked policy data.
     * Derives ML input features from existing claim/policy fields.
     */
    @PostMapping("/{id}/segment")
    public ResponseEntity<SegmentationResponseDTO> segmentClaim(@PathVariable Long id) {
        return ResponseEntity.ok(segmentationService.segmentFromClaim(id));
    }

    /**
     * Direct segmentation with a fully-provided input (for admin manual analysis).
     */
    @PostMapping("/segment")
    public ResponseEntity<SegmentationResponseDTO> segmentDirect(@RequestBody SegmentationRequestDTO request) {
        return ResponseEntity.ok(segmentationService.segmentDirect(request));
    }

    @GetMapping
    public ResponseEntity<List<InsuranceClaimDTO>> getAll() {
        List<InsuranceClaimDTO> dtos = claimService.getAll()
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        claimService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
