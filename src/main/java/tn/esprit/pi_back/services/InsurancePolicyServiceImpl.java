package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.PolicyStatus;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.VoucherStatus;
import tn.esprit.pi_back.repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsurancePolicyServiceImpl implements IInsurancePolicyService {

    private final InsurancePolicyRepository policyRepository;
    private final InsuranceOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final TransactionRepository transactionRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InsurancePolicy createContract(Long clientId, Long offerId, Double declaredValue, String goodsDescription, String voucherCode, LocalDate startDate) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        InsuranceOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        InsurancePolicy policy = new InsurancePolicy();
        policy.setClient(client);
        policy.setInsuranceOffer(offer);
        
        // Sécurité : Vérifier si l'assureur existe pour cette offre
        if (offer.getInsuranceCompany() != null) {
            policy.setInsuranceCompany(offer.getInsuranceCompany());
        }

        // VALIDATION : La date de début ne peut pas être dans le passé
        if (startDate != null && startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("La date de début du contrat ne peut pas être dans le passé.");
        }

        policy.setPolicyNumber(generatePolicyNumber());
        policy.setStartDate(startDate != null ? startDate : LocalDate.now());
        policy.setEndDate(policy.getStartDate().plusYears(1));
        policy.setStatus(PolicyStatus.ACTIF); 
        policy.setPremiumAmount(offer.getAnnualPremium());
        policy.setDeclaredValue(declaredValue);
        policy.setGoodsDescription(goodsDescription);
        policy.setDurationYears(1);
        policy.setPdfUrl("https://crediguard.storage/contracts/" + policy.getPolicyNumber() + ".pdf");

        try {
            InsurancePolicy saved = policyRepository.save(policy);
            
            // DYNAMIC NOTIFICATION
            notificationService.createNotification(
                client,
                saved.getPolicyNumber(),
                "Votre nouveau contrat d'assurance est actif.",
                saved.getPolicyNumber(),
                tn.esprit.pi_back.entities.enums.NotificationCategory.CONTRAT,
                "Actif",
                (saved.getGoodsDescription() != null ? saved.getGoodsDescription() : "N/A") + 
                (client.getRegion() != null ? " - " + client.getRegion() : "")
            );
            
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du contrat : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public InsurancePolicy renewContract(Long policyId) {
        InsurancePolicy oldPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
        
        InsurancePolicy newPolicy = createContract(
                oldPolicy.getClient().getId(),
                oldPolicy.getInsuranceOffer().getId(),
                oldPolicy.getDeclaredValue(),
                oldPolicy.getGoodsDescription(),
                null, // Pas de voucherCode nécessaire pour un renouvellement direct
                LocalDate.now() // Date de début pour le renouvellement
        );
        
        oldPolicy.setStatus(PolicyStatus.EXPIRE);
        policyRepository.save(oldPolicy);
        
        // DYNAMIC NOTIFICATION for Renewal
        notificationService.createNotification(
            newPolicy.getClient(),
            newPolicy.getPolicyNumber(),
            "Votre contrat a été renouvelé avec succès.",
            newPolicy.getPolicyNumber(),
            tn.esprit.pi_back.entities.enums.NotificationCategory.CONTRAT,
            "Renouvellement",
            "Nouveau numéro: " + newPolicy.getPolicyNumber()
        );
        
        return newPolicy;
    }

    @Override
    public List<InsurancePolicy> getClientPolicies(Long clientId) {
        return policyRepository.findByClientId(clientId);
    }

    @Override
    public InsurancePolicy getById(Long id) {
        return policyRepository.findById(id).orElseThrow(() -> new RuntimeException("Contrat non trouvé"));
    }

    @Override
    public void checkExpiringPolicies() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<InsurancePolicy> expiring = policyRepository.findByEndDateBetween(LocalDate.now(), thirtyDaysFromNow);
        
        for (InsurancePolicy p : expiring) {
            System.out.println("Alert: Policy " + p.getPolicyNumber() + " expires soon!");
        }
    }

    // Legacy implementations
    @Override
    public InsurancePolicy addAndAssign(Long companyId, Long clientId, InsurancePolicy policy) {
        InsuranceCompany company = companyRepository.findById(companyId).orElse(null);
        User client = userRepository.findById(clientId).orElse(null);
        if (company == null || client == null) return null;
        
        policy.setInsuranceCompany(company);
        policy.setClient(client);
        if (policy.getPolicyNumber() == null) policy.setPolicyNumber(generatePolicyNumber());
        if (policy.getStatus() == null) policy.setStatus(PolicyStatus.PENDING);
        
        return policyRepository.save(policy);
    }

    @Override
    public InsurancePolicy update(InsurancePolicy policy) {
        if (!policyRepository.existsById(policy.getId())) return null;
        return policyRepository.save(policy);
    }

    @Override
    public void delete(Long id) {
        policyRepository.deleteById(id);
    }

    @Override
    public InsurancePolicy get(Long id) {
        return policyRepository.findById(id).orElse(null);
    }

    @Override
    public List<InsurancePolicy> all() {
        return policyRepository.findAll();
    }

    @Override
    public InsurancePolicy getPolicyByUserId(Long userId) {
        List<InsurancePolicy> policies = policyRepository.findByClientId(userId);
        return policies.isEmpty() ? null : policies.get(0);
    }

    @Override
    public List<InsurancePolicy> getPoliciesByUserId(Long userId) {
        return policyRepository.findByClientId(userId);
    }

    private String generatePolicyNumber() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("POL-");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String num = sb.toString();
        if (policyRepository.existsByPolicyNumber(num)) {
            return generatePolicyNumber();
        }
        return num;
    }
}