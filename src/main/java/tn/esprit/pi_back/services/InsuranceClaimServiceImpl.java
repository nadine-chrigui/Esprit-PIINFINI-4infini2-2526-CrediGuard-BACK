package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.ClaimStatus;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.VoucherStatus;
import tn.esprit.pi_back.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InsuranceClaimServiceImpl implements IInsuranceClaimService {

    private final InsuranceClaimRepository claimRepository;
    private final ClaimHistoryRepository historyRepository;
    private final InsurancePolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InsuranceClaim submitClaim(Long voucherId, Long policyId, Long userId, String description, String claimReference, List<MultipartFile> files, Double amountRequested) {
        InsurancePolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Contrat non trouvé"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        InsuranceClaim claim = new InsuranceClaim();
        claim.setInsurancePolicy(policy);
        claim.setUser(user);
        claim.setDescription(description);
        claim.setClaimReference(claimReference);
        claim.setAmountRequested(amountRequested);
        claim.setClaimNumber(generateClaimNumber(voucherId));
        claim.setStatus(ClaimStatus.PENDING);
        claim.setFraudScore(new Random().nextInt(101));
        claim.setRiskScore(new Random().nextInt(101));

        // Lier le voucher si présent
        if (voucherId != null) {
            if (claimRepository.existsByVoucherId(voucherId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, 
                    "Un sinistre a déjà été déclaré pour ce voucher (ID: " + voucherId + ")"
                );
            }
            voucherRepository.findById(voucherId).ifPresent(claim::setVoucher);
        }

        // Mock File Upload
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                urls.add("https://s3.amazonaws.com/crediguard/claims/" + claim.getClaimNumber() + "/" + file.getOriginalFilename());
            }
        }
        claim.setDocumentsUrl(urls);

        InsuranceClaim saved;
        try {
            saved = claimRepository.saveAndFlush(claim);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Erreur de duplication : Ce sinistre (ou voucher) est déjà enregistré."
                );
            }
            throw e;
        }

        // Log history (wrapped in try-catch to be non-blocking)
        try {
            logHistory(saved, null, ClaimStatus.PENDING, "Initial submission via Partnership", user);
            
            // DYNAMIC NOTIFICATION
            notificationService.createNotification(
                user, 
                saved.getClaimNumber(), 
                "Votre claim a été soumis avec succès.", 
                saved.getClaimNumber(), 
                tn.esprit.pi_back.entities.enums.NotificationCategory.CLAIM,
                "Soumis", 
                "En attente de validation par l'assureur."
            );
        } catch (Exception e) {
            System.err.println("History log/Notification failed but claim was saved: " + e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public InsuranceClaim updateStatus(Long claimId, ClaimStatus status, String comment, Double amountApproved, String rejectionReason, Long adminId) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sinistre non trouvé"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Admin non trouvé with ID: " + adminId));

        ClaimStatus oldStatus = claim.getStatus();
        
        // FIX 1: If claimNumber is missing (legacy data), generate one
        if (claim.getClaimNumber() == null || claim.getClaimNumber().trim().isEmpty()) {
            claim.setClaimNumber(generateClaimNumber(claim.getVoucher() != null ? claim.getVoucher().getId() : null));
        }

        // FIX 2: If user is missing (orphaned data), assign owner to satisfy DB NOT NULL constraint
        if (claim.getUser() == null) {
            if (claim.getInsurancePolicy() != null && claim.getInsurancePolicy().getClient() != null) {
                claim.setUser(claim.getInsurancePolicy().getClient());
            } else {
                claim.setUser(admin); 
            }
        }

        claim.setStatus(status);
        claim.setDecidedAt(LocalDateTime.now());
        
        if (status == ClaimStatus.APPROVED) {
            claim.setAmountApproved(amountApproved != null ? amountApproved : 0.0);
            // LOGIQUE DE REMBOURSEMENT SUPPRIMÉE : L'assurance est strictement décisionnelle.
            // Le module Crédit gérera indépendamment ses flux de vouchers.
        } else if (status == ClaimStatus.REJECTED) {
            claim.setRejectionReason(rejectionReason);
        }

        InsuranceClaim saved = claimRepository.saveAndFlush(claim);
        logHistory(saved, oldStatus, status, comment, admin);

        // DYNAMIC NOTIFICATION
        String statusText = (status == ClaimStatus.APPROVED) ? "Validée" : (status == ClaimStatus.REJECTED ? "Refusée" : "En cours");
        String messageText = (status == ClaimStatus.APPROVED) ? "Votre claim a été validé par l'assureur." : (status == ClaimStatus.REJECTED ? "Votre claim a été refusé." : "Votre dossier est en cours d'examen.");
        String detailsText = (status == ClaimStatus.APPROVED) ? "Montant couvert : " + saved.getAmountApproved() + " TND" : (status == ClaimStatus.REJECTED ? "Motif : " + saved.getRejectionReason() : "Examen par l'expert.");

        notificationService.createNotification(
            claim.getUser(),
            saved.getClaimNumber(),
            messageText,
            saved.getClaimNumber(),
            tn.esprit.pi_back.entities.enums.NotificationCategory.CLAIM,
            statusText,
            detailsText
        );

        return saved;
    }

    private void logHistory(InsuranceClaim claim, ClaimStatus oldStatus, ClaimStatus newStatus, String comment, User user) {
        ClaimHistory history = new ClaimHistory();
        history.setClaim(claim);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setComment(comment);
        history.setChangedBy(user);
        historyRepository.save(history);
    }

    @Override
    public List<InsuranceClaim> getByClient(Long clientId) {
        return claimRepository.findByUserId(clientId);
    }

    @Override
    public InsuranceClaim getById(Long id) {
        return claimRepository.findById(id).orElseThrow(() -> new RuntimeException("Sinistre non trouvé"));
    }

    @Override
    public List<InsuranceClaim> getAll() {
        return claimRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        claimRepository.deleteById(id);
    }

    private String generateClaimNumber(Long voucherId) {
        int year = LocalDate.now().getYear();
        long timestamp = System.currentTimeMillis() % 100000; // Last 5 digits of timestamp
        int random = new Random().nextInt(900) + 100; // 3 digit random
        return "CLM-" + year + "-" + (voucherId != null ? voucherId : "X") + "-" + timestamp + "-" + random;
    }
}