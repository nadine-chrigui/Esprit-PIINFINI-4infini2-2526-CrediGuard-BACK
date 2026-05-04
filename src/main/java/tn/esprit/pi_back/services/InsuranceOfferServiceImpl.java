package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.insurance.InsuranceMapper;
import tn.esprit.pi_back.dto.insurance.InsuranceOfferDTO;
import tn.esprit.pi_back.dto.insurance.RecommendedOfferDTO;
import tn.esprit.pi_back.entities.InsuranceOffer;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.InsuranceOfferRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceOfferServiceImpl implements IInsuranceOfferService {

    private final InsuranceOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final AdequacyService adequacyService;

    @jakarta.annotation.PostConstruct
    public void initMockData() {
        List<InsuranceOffer> offers = offerRepository.findAll();
        boolean changed = false;
        for (InsuranceOffer o : offers) {
            boolean rowChanged = false;

            // FIX POUR LE TYPE VIDE (évite le crash au démarrage)
            if (o.getType() == null || o.getType().trim().isEmpty()) {
                o.setType("PROPERTY");
                rowChanged = true;
            }

            // Si les données sont à 0 ou null, on injecte du réalisme varié
            if (o.getFranchise() == null || o.getFranchise() == 0) {
                long id = o.getId() != null ? o.getId() : 1;
                double price = o.getAnnualPremium() != null ? o.getAnnualPremium() : 0;
                
                if (price > 1000) {
                    o.setFranchise(0.0);
                    o.setCoverageRate(100);
                } else if (price > 400) {
                    o.setFranchise(50.0 + (id % 5) * 10); // Varie entre 50 et 90
                    o.setCoverageRate(95);
                } else {
                    o.setFranchise(100.0 + (id % 10) * 15); // Varie entre 100 et 250
                    o.setCoverageRate(75 + (int)(id % 5) * 5); // Varie entre 75% et 95%
                }
                rowChanged = true;
            }

            if (rowChanged) {
                offerRepository.save(o);
                changed = true;
            }
        }
        if (changed) System.out.println("✅ CrediGuard : Données d'offres initialisées et types corrigés !");
    }

    @Override
    public List<InsuranceOfferDTO> getAll() {
        return offerRepository.findAll().stream()
                .map(InsuranceMapper::toOfferDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedOfferDTO> getRecommended(Long clientId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        List<InsuranceOffer> activeOffers = offerRepository.findByActiveTrue();
        
        return activeOffers.stream()
                .map(offer -> {
                    RecommendedOfferDTO dto = new RecommendedOfferDTO();
                    // Map basic fields
                    InsuranceOfferDTO basic = InsuranceMapper.toOfferDTO(offer);
                    dto.setId(basic.getId());
                    dto.setName(basic.getName());
                    dto.setAnnualPremium(basic.getAnnualPremium());
                    dto.setCoverageDetails(basic.getCoverageDetails());
                    dto.setGuarantees(basic.getGuarantees());
                    dto.setExclusions(basic.getExclusions());
                    dto.setType(basic.getType());
                    dto.setCoverageAmount(basic.getCoverageAmount());
                    dto.setFranchise(basic.getFranchise());
                    dto.setCoverageRate(basic.getCoverageRate());
                    dto.setTags(basic.getTags());
                    dto.setActive(basic.isActive());
                    dto.setCompanyId(basic.getCompanyId());
                    dto.setCompanyName(basic.getCompanyName());
                    
                    // Calculate adequacy score
                    dto.setAdequacyScore(adequacyService.calculateScore(offer, client));
                    return dto;
                })
                .sorted(Comparator.comparingInt(RecommendedOfferDTO::getAdequacyScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public InsuranceOffer save(InsuranceOffer offer) {
        // RÈGLES DE CLASSIFICATION AUTOMATIQUE
        if (offer.getAnnualPremium() != null) {
            double premium = offer.getAnnualPremium();
            if (premium < 250) {
                offer.setTags("BASIC, low_cost, basic_cover");
            } else if (premium <= 500) {
                offer.setTags("STANDARD, balanced, recommended");
            } else {
                offer.setTags("PREMIUM, full_cover, vip, fast_claim");
            }
        }
        return offerRepository.save(offer);
    }

    @Override
    public InsuranceOffer update(Long id, InsuranceOffer offer) {
        InsuranceOffer existing = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        
        existing.setName(offer.getName());
        existing.setAnnualPremium(offer.getAnnualPremium());
        existing.setCoverageDetails(offer.getCoverageDetails());
        existing.setGuarantees(offer.getGuarantees());
        existing.setExclusions(offer.getExclusions());
        existing.setType(offer.getType());
        existing.setCoverageAmount(offer.getCoverageAmount());
        existing.setFranchise(offer.getFranchise());
        existing.setCoverageRate(offer.getCoverageRate());
        
        // RE-CALCULER LES TAGS SI LE PRIX CHANGE
        if (offer.getAnnualPremium() != null) {
            double p = offer.getAnnualPremium();
            if (p < 250) existing.setTags("BASIC, low_cost, basic_cover");
            else if (p <= 500) existing.setTags("STANDARD, balanced, recommended");
            else existing.setTags("PREMIUM, full_cover, vip, fast_claim");
        }
        
        existing.setActive(offer.isActive());
        
        return offerRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        InsuranceOffer existing = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
        existing.setActive(false);
        offerRepository.save(existing);
    }

    @Override
    public InsuranceOffer getById(Long id) {
        return offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Offre non trouvée"));
    }
}
