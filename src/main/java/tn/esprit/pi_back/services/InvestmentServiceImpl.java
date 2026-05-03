package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentOfferRepository offerRepo;
    private final PerformanceTrackingRepository performanceRepo;
    private final ScoringService scoringService;
    private final EmailService emailService;

    @Override
    public List<InvestmentOffer> getOffersForUser(Long userId) {
        List<InvestmentOffer> existing = offerRepo.findByUserIdAndStatus(userId, OfferStatus.PROPOSED);
        if (!existing.isEmpty()) return existing;

        FinancialProfile profile = scoringService.getProfile(userId);
        User user = profile.getUser();

        List<InvestmentOffer> generated = new ArrayList<>();

        if (profile.getProfileType() == ProfileType.PRUDENT) {
            generated.add(InvestmentOffer.builder()
                    .user(user)
                    .title("Épargne Sécurisée")
                    .description("Placements à capital protégé (fonds garantis, livrets à taux fixe). L'objectif : stabiliser votre épargne et améliorer votre score.")
                    .type(InvestmentType.EPARGNE)
                    .riskLevel("Faible")
                    .estimatedReturn(3.5)
                    .status(OfferStatus.PROPOSED)
                    .build());
        } else if (profile.getProfileType() == ProfileType.EQUILIBRE) {
            generated.add(InvestmentOffer.builder()
                    .user(user)
                    .title("Micro-investissement")
                    .description("Investissement diversifié en Actions fractionnées et ETF. Faites travailler votre argent avec un risque maîtrisé.")
                    .type(InvestmentType.MICRO_INVEST)
                    .riskLevel("Modéré")
                    .estimatedReturn(8.2)
                    .status(OfferStatus.PROPOSED)
                    .build());
        } else {
            generated.add(InvestmentOffer.builder()
                    .user(user)
                    .title("Crédit Rentable")
                    .description("Levier de croissance : financement de stock ou matériel pour votre commerce. Transformez le crédit en profit.")
                    .type(InvestmentType.CREDIT_RENTABLE)
                    .riskLevel("Elevé")
                    .estimatedReturn(15.0)
                    .status(OfferStatus.PROPOSED)
                    .build());
        }

        return offerRepo.saveAll(generated);
    }

    @Override
    public InvestmentOffer acceptOffer(Long offerId) {
        InvestmentOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setStatus(OfferStatus.ACCEPTED);

        PerformanceTracking initial = PerformanceTracking.builder()
                .investmentOffer(offer)
                .value(1000.0)
                .variation(0.0)
                .date(LocalDateTime.now())
                .build();
        performanceRepo.save(initial);

        try {
            String toEmail = offer.getUser().getEmail();
            if (toEmail != null && !toEmail.isEmpty()) {
                String subject = "Félicitations ! Votre souscription CrediGuard est confirmée";
                String htmlBody = "<html><body style='font-family: sans-serif; color: #333;'>" +
                        "<div style='max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px; border-radius: 10px;'>" +
                        "<h2 style='color: #0fd674;'>CrediGuard — Brain Financier</h2>" +
                        "<p>Bonjour <strong>" + offer.getUser().getFullName() + "</strong>,</p>" +
                        "<p>Nous avons le plaisir de vous confirmer votre souscription à l'offre :</p>" +
                        "<div style='background: #f9f9f9; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                        "<h3 style='margin-top: 0;'>" + offer.getTitle() + "</h3>" +
                        "<p>" + offer.getDescription() + "</p>" +
                        "<p><strong>Rendement estimé :</strong> " + offer.getEstimatedReturn() + "% par an</p>" +
                        "</div>" +
                        "<p>Vous pouvez suivre l'évolution de votre investissement en temps réel sur votre tableau de bord.</p>" +
                        "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                        "<p style='font-size: 0.8rem; color: #999;'>Ceci est un message automatique, merci de ne pas y répondre.</p>" +
                        "</div></body></html>";
                emailService.sendEmail(toEmail, subject, htmlBody);
            }
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return offerRepo.save(offer);
    }

    @Override
    public List<PerformanceTracking> getPerformance(Long offerId) {
        return performanceRepo.findByInvestmentOfferIdOrderByDateAsc(offerId);
    }

    @Override
    public void updatePerformance(Long offerId, double newValue) {
        InvestmentOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        List<PerformanceTracking> history = performanceRepo.findByInvestmentOfferIdOrderByDateAsc(offerId);
        double prevValue = history.isEmpty() ? newValue : history.get(history.size() - 1).getValue();
        double variation = prevValue == 0 ? 0 : ((newValue - prevValue) / prevValue) * 100;

        PerformanceTracking tracking = PerformanceTracking.builder()
                .investmentOffer(offer)
                .value(newValue)
                .variation(variation)
                .date(LocalDateTime.now())
                .build();
        performanceRepo.save(tracking);
    }
}