package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.evaluation.LigneAmortissementDTO;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.Modalite;
import tn.esprit.pi_back.entities.enums.TypeModalite;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.ModaliteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModaliteAmortissementService {

    private final ModaliteRepository modaliteRepository;

    public List<LigneAmortissementDTO> getTableau(Long demandeId) {
        Modalite modalite = modaliteRepository.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Modalite not found"));

        return construireTableau(modalite);
    }

    private List<LigneAmortissementDTO> construireTableau(Modalite modalite) {
        DemandeCredit demande = modalite.getDemandeCredit();

        TypeModalite type = modalite.getModaliteChoisie() != null
                ? modalite.getModaliteChoisie()
                : modalite.getModaliteRecommandee();

        if (type == TypeModalite.REFUS) {
            throw new IllegalStateException("Cannot generate amortization table for refused modalite");
        }

        BigDecimal capitalInitial = BigDecimal.valueOf(demande.getMontantDemande())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal capitalRestant = capitalInitial;

        BigDecimal tauxAnnuel = BigDecimal.valueOf(modalite.getTauxInteretAnnuel())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        int dureeMois = demande.getDureeMois();

        int moisGrace = Boolean.TRUE.equals(modalite.getGraceActive())
                ? safeInt(modalite.getDureeGraceMois())
                : 0;

        moisGrace = Math.min(Math.max(moisGrace, 0), Math.max(dureeMois - 1, 0));

        int graceEffective = moisGrace;
        int dureeEffective = Math.max(1, dureeMois - graceEffective);

        List<LigneAmortissementDTO> lignes = new ArrayList<>();

        LocalDate datePaiementPrecedente = LocalDate.now();
        LocalDate premiereDatePaiement = datePaiementPrecedente.plusMonths(1);

        int numero = 1;

        for (int i = 0; i < graceEffective; i++) {
            LocalDate datePaiement = premiereDatePaiement.plusMonths(i);

            BigDecimal interetCouru = calculerInteretCouru(
                    capitalRestant,
                    tauxAnnuel,
                    datePaiementPrecedente,
                    datePaiement
            );

            lignes.add(new LigneAmortissementDTO(
                    numero++,
                    datePaiement,
                    "GRACE",
                    interetCouru.doubleValue(),
                    interetCouru.doubleValue(),
                    0.0,
                    capitalRestant.doubleValue()
            ));

            datePaiementPrecedente = datePaiement;
        }

        if (type == TypeModalite.AMORTISSABLE) {
            BigDecimal capitalConstant = capitalInitial
                    .divide(BigDecimal.valueOf(dureeEffective), 2, RoundingMode.HALF_UP);

            for (int i = 0; i < dureeEffective; i++) {
                LocalDate datePaiement = premiereDatePaiement.plusMonths(graceEffective + i);

                BigDecimal interetCouru = calculerInteretCouru(
                        capitalRestant,
                        tauxAnnuel,
                        datePaiementPrecedente,
                        datePaiement
                );

                BigDecimal capitalRembourse = i == dureeEffective - 1
                        ? capitalRestant
                        : capitalConstant;

                BigDecimal mensualite = capitalRembourse
                        .add(interetCouru)
                        .setScale(2, RoundingMode.HALF_UP);

                capitalRestant = capitalRestant
                        .subtract(capitalRembourse)
                        .max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);

                lignes.add(new LigneAmortissementDTO(
                        numero++,
                        datePaiement,
                        "AMORTISSEMENT_CONSTANT",
                        mensualite.doubleValue(),
                        interetCouru.doubleValue(),
                        capitalRembourse.doubleValue(),
                        capitalRestant.doubleValue()
                ));

                datePaiementPrecedente = datePaiement;
            }
        }

        if (type == TypeModalite.IN_FINE) {
            for (int i = 0; i < dureeEffective; i++) {
                LocalDate datePaiement = premiereDatePaiement.plusMonths(graceEffective + i);

                BigDecimal interetCouru = calculerInteretCouru(
                        capitalRestant,
                        tauxAnnuel,
                        datePaiementPrecedente,
                        datePaiement
                );

                BigDecimal capitalRembourse = i == dureeEffective - 1
                        ? capitalRestant
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal mensualite = interetCouru
                        .add(capitalRembourse)
                        .setScale(2, RoundingMode.HALF_UP);

                capitalRestant = capitalRestant
                        .subtract(capitalRembourse)
                        .max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP);

                lignes.add(new LigneAmortissementDTO(
                        numero++,
                        datePaiement,
                        "IN_FINE",
                        mensualite.doubleValue(),
                        interetCouru.doubleValue(),
                        capitalRembourse.doubleValue(),
                        capitalRestant.doubleValue()
                ));

                datePaiementPrecedente = datePaiement;
            }
        }

        return lignes;
    }

    private BigDecimal calculerInteretCouru(
            BigDecimal capitalRestantDu,
            BigDecimal tauxAnnuel,
            LocalDate datePaiementPrecedente,
            LocalDate datePaiementActuelle
    ) {
        long nombreJours = ChronoUnit.DAYS.between(
                datePaiementPrecedente,
                datePaiementActuelle
        ) + 1;

        if (nombreJours < 0) {
            nombreJours = 0;
        }

        BigDecimal interetJournalier = capitalRestantDu
                .multiply(tauxAnnuel)
                .divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_UP);

        return interetJournalier
                .multiply(BigDecimal.valueOf(nombreJours))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
