package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.evaluation.ModaliteRequestDTO;
import tn.esprit.pi_back.dto.evaluation.ModaliteResponseDTO;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.EvaluationRisque;
import tn.esprit.pi_back.entities.Modalite;
import tn.esprit.pi_back.entities.ProfilCredit;
import tn.esprit.pi_back.entities.enums.DecisionModalite;
import tn.esprit.pi_back.entities.enums.TypeModalite;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.EvaluationRisqueRepository;
import tn.esprit.pi_back.repositories.ModaliteRepository;
import tn.esprit.pi_back.repositories.ProfilCreditRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class ModaliteServiceImpl implements ModaliteService {

    private static final double FIXED_INTEREST_RATE = 0.30;

    private final ModaliteRepository modaliteRepo;
    private final DemandeCreditRepository demandeRepo;
    private final ProfilCreditRepository profilRepo;
    private final EvaluationRisqueRepository evaluationRepo;

    @Override
    public ModaliteResponseDTO generate(Long demandeId) {
        Context c = loadContext(demandeId);

        double loanAmount = c.demande.getMontantDemande();
        int nTotal = c.demande.getDureeMois();

        int graceMonths = Math.min(
                Math.max(safeInt(c.profil.getProjectStartDelayMonths()), 0),
                Math.max(nTotal - 1, 0)
        );

        int effectiveMonths = Math.max(1, nTotal - graceMonths);

        double revenuMensuel = c.profil.getPersonIncomeAnnual() / 12.0;

        double revenuFutur = safe(c.profil.getExpectedMonthlyRevenueAfterStart())
                * (Boolean.TRUE.equals(c.profil.getHasExistingClients()) ? 0.70 : 0.30);

        double revenuTotal = graceMonths == 0
                ? revenuMensuel + revenuFutur
                : revenuMensuel;

        double charges = safe(c.profil.getMonthlyFixedCharges())
                + safe(c.profil.getExistingLoanMonthlyPayments())
                + safe(c.profil.getOutstandingOldDebt()) / 36.0;

        double capaciteMax = Math.max(0.0, revenuTotal * 0.50 - charges);

        double mensualiteInFine = firstInFinePayment(loanAmount);
        double mensualiteAmortissable = firstAmortissementConstantPayment(loanAmount, effectiveMonths);

        double pd = pd(c.evaluation);
        double scoreCredit = (1.0 - pd) * 100.0;

        TypeModalite recommandee;
        DecisionModalite decision;
        String motif;

        if (pd > 0.60) {
            recommandee = TypeModalite.REFUS;
            decision = DecisionModalite.REFUS;
            motif = "Risque ML critique : PD/VaR95 superieur a 60%.";
        } else if (mensualiteAmortissable <= capaciteMax
                && safe(c.profil.getPersonEmploymentLength()) >= 1.0
                && pd <= 0.50) {
            recommandee = TypeModalite.AMORTISSABLE;
            decision = DecisionModalite.ACCEPTER;
            motif = "Capacite suffisante pour une modalite a amortissement constant.";
        } else if (mensualiteInFine <= capaciteMax) {
            recommandee = TypeModalite.IN_FINE;
            decision = DecisionModalite.CONDITIONNEL;
            motif = "Capacite limitee : modalite in fine recommandee.";
        } else {
            recommandee = TypeModalite.REFUS;
            decision = DecisionModalite.REFUS;
            motif = "Capacite de remboursement insuffisante.";
        }

        boolean graceActive = graceMonths > 0 && decision != DecisionModalite.REFUS;

        double dti = c.profil.getPersonIncomeAnnual() > 0
                ? loanAmount / c.profil.getPersonIncomeAnnual()
                : 0.0;

        double paymentToIncome = revenuTotal > 0
                ? mensualiteAmortissable / revenuTotal
                : 0.0;

        double lti = revenuTotal > 0
                ? loanAmount / (revenuTotal * 12.0)
                : 0.0;

        boolean stress = dti > 0.40;

        Modalite modalite = modaliteRepo.findByDemandeCreditId(demandeId)
                .orElseGet(Modalite::new);

        modalite.setDemandeCredit(c.demande);
        modalite.setEvaluationRisque(c.evaluation);

        modalite.setModaliteRecommandee(recommandee);

        if (modalite.getModaliteChoisie() == null) {
            modalite.setModaliteChoisie(recommandee == TypeModalite.REFUS ? null : recommandee);
        }

        modalite.setDecision(decision);
        modalite.setMotif(motif);
        modalite.setTauxInteretAnnuel(FIXED_INTEREST_RATE);

        modalite.setRevenuMensuelActuel(round(revenuMensuel));
        modalite.setRevenuFuturReconnu(round(revenuFutur));
        modalite.setRevenuTotalReconnu(round(revenuTotal));
        modalite.setChargesMensuellesTotales(round(charges));
        modalite.setCapaciteMensuelleMax(round(capaciteMax));

        modalite.setMensualiteAmortissable(round(mensualiteAmortissable));
        modalite.setMensualiteInFine(round(mensualiteInFine));
        modalite.setMensualiteGrace(round(graceActive ? mensualiteInFine : 0.0));

        modalite.setGraceActive(graceActive);
        modalite.setDureeGraceMois(graceActive ? graceMonths : 0);
        modalite.setDureeEffectiveMois(graceActive ? effectiveMonths : nTotal);

        modalite.setProbabiliteDefaut(round(pd));
        modalite.setVar95(round(safe(c.evaluation.getVar95())));
        modalite.setCvar95(round(safe(c.evaluation.getCvar95())));
        modalite.setScoreCredit(round(scoreCredit));

        modalite.setNiveauRisque(
                c.evaluation.getNiveauRisque() != null
                        ? c.evaluation.getNiveauRisque().name()
                        : null
        );

        modalite.setDti(round(dti));
        modalite.setPaymentToIncome(round(paymentToIncome));
        modalite.setLti(round(lti));
        modalite.setFinancialStress(stress);

        modalite.setCoutTotalAmortissable(
                round((graceActive ? mensualiteInFine * graceMonths : 0.0)
                        + totalCostAmortissementConstant(loanAmount, effectiveMonths))
        );

        modalite.setCoutTotalInFine(
                round(totalCostInFine(loanAmount, nTotal))
        );

        return toDTO(modaliteRepo.save(modalite));
    }

    @Override
    public ModaliteResponseDTO choose(Long demandeId, ModaliteRequestDTO dto) {
        Modalite modalite = modaliteRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Modalite not found. Generate modalite first."));

        if (modalite.getDecision() == DecisionModalite.REFUS
                && dto.modaliteChoisie() != TypeModalite.REFUS) {
            throw new IllegalStateException("Cannot choose a payment modalite when recommendation is REFUS");
        }

        modalite.setModaliteChoisie(dto.modaliteChoisie());
        modalite.setCommentaireAdmin(dto.commentaireAdmin());
        modalite.setChoisiePar(dto.choisiePar());
        modalite.setDateChoix(LocalDateTime.now());

        return toDTO(modaliteRepo.save(modalite));
    }

    @Override
    @Transactional(readOnly = true)
    public ModaliteResponseDTO getByDemande(Long demandeId) {
        return modaliteRepo.findByDemandeCreditId(demandeId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Modalite not found"));
    }

    private Context loadContext(Long demandeId) {
        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        ProfilCredit profil = profilRepo.findByClientId(demande.getClient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil credit not found"));

        EvaluationRisque evaluation = evaluationRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation risk not found. Run ML prediction first."));

        return new Context(demande, profil, evaluation);
    }

    private double firstAmortissementConstantPayment(double amount, int months) {
        double capitalConstant = months > 0 ? amount / months : amount;
        double firstInterest = accruedInterest(amount, LocalDate.now(), LocalDate.now().plusMonths(1));

        return capitalConstant + firstInterest;
    }

    private double firstInFinePayment(double amount) {
        return accruedInterest(amount, LocalDate.now(), LocalDate.now().plusMonths(1));
    }

    private double totalCostAmortissementConstant(double amount, int months) {
        if (months <= 0) return 0.0;

        LocalDate previousDate = LocalDate.now();
        LocalDate firstPaymentDate = previousDate.plusMonths(1);

        double capitalRestant = amount;
        double capitalConstant = amount / months;
        double total = 0.0;

        for (int i = 0; i < months; i++) {
            LocalDate paymentDate = firstPaymentDate.plusMonths(i);

            double interest = accruedInterest(capitalRestant, previousDate, paymentDate);
            double capitalPaid = (i == months - 1) ? capitalRestant : capitalConstant;
            double payment = capitalPaid + interest;

            total += payment;
            capitalRestant = Math.max(0.0, capitalRestant - capitalPaid);
            previousDate = paymentDate;
        }

        return total;
    }

    private double totalCostInFine(double amount, int months) {
        if (months <= 0) return 0.0;

        LocalDate previousDate = LocalDate.now();
        LocalDate firstPaymentDate = previousDate.plusMonths(1);

        double total = 0.0;

        for (int i = 0; i < months; i++) {
            LocalDate paymentDate = firstPaymentDate.plusMonths(i);
            double interest = accruedInterest(amount, previousDate, paymentDate);

            total += interest;

            if (i == months - 1) {
                total += amount;
            }

            previousDate = paymentDate;
        }

        return total;
    }

    private double accruedInterest(double capitalRestantDu, LocalDate previousPaymentDate, LocalDate currentPaymentDate) {
        long numberOfDays = ChronoUnit.DAYS.between(previousPaymentDate, currentPaymentDate) + 1;

        if (numberOfDays < 0) {
            numberOfDays = 0;
        }

        double dailyInterest = capitalRestantDu * (FIXED_INTEREST_RATE / 100.0) / 360.0;

        return dailyInterest * numberOfDays;
    }

    private double pd(EvaluationRisque evaluation) {
        if (evaluation.getVar95() != null) {
            return evaluation.getVar95();
        }

        if (evaluation.getProbabiliteDefautConservative() != null) {
            return evaluation.getProbabiliteDefautConservative();
        }

        return evaluation.getProbabiliteDefaut() != null
                ? evaluation.getProbabiliteDefaut()
                : 1.0;
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private ModaliteResponseDTO toDTO(Modalite modalite) {
        return new ModaliteResponseDTO(
                modalite.getId(),
                modalite.getDemandeCredit() != null
                        ? modalite.getDemandeCredit().getId()
                        : null,
                modalite.getEvaluationRisque() != null
                        ? modalite.getEvaluationRisque().getId()
                        : null,

                modalite.getModaliteRecommandee(),
                modalite.getModaliteChoisie(),
                modalite.getDecision(),
                modalite.getMotif(),

                modalite.getTauxInteretAnnuel(),

                modalite.getRevenuMensuelActuel(),
                modalite.getRevenuFuturReconnu(),
                modalite.getRevenuTotalReconnu(),
                modalite.getChargesMensuellesTotales(),
                modalite.getCapaciteMensuelleMax(),

                modalite.getMensualiteAmortissable(),
                modalite.getMensualiteInFine(),
                modalite.getMensualiteGrace(),

                modalite.getGraceActive(),
                modalite.getDureeGraceMois(),
                modalite.getDureeEffectiveMois(),

                modalite.getProbabiliteDefaut(),
                modalite.getVar95(),
                modalite.getCvar95(),
                modalite.getScoreCredit(),
                modalite.getNiveauRisque(),

                modalite.getDti(),
                modalite.getPaymentToIncome(),
                modalite.getLti(),
                modalite.getFinancialStress(),

                modalite.getCoutTotalAmortissable(),
                modalite.getCoutTotalInFine(),

                modalite.getCommentaireAdmin(),
                modalite.getChoisiePar(),
                modalite.getDateChoix(),

                modalite.getCreatedAt(),
                modalite.getUpdatedAt()
        );
    }

    private record Context(
            DemandeCredit demande,
            ProfilCredit profil,
            EvaluationRisque evaluation
    ) {}
}
