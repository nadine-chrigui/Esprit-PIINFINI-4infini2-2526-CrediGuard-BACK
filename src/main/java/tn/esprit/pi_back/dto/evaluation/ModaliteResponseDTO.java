package tn.esprit.pi_back.dto.evaluation;

import tn.esprit.pi_back.entities.enums.DecisionModalite;
import tn.esprit.pi_back.entities.enums.TypeModalite;

import java.time.LocalDateTime;

public record ModaliteResponseDTO(
        Long id,
        Long demandeId,
        Long evaluationId,

        TypeModalite modaliteRecommandee,
        TypeModalite modaliteChoisie,
        DecisionModalite decision,
        String motif,

        Double tauxInteretAnnuel,

        Double revenuMensuelActuel,
        Double revenuFuturReconnu,
        Double revenuTotalReconnu,
        Double chargesMensuellesTotales,
        Double capaciteMensuelleMax,

        Double mensualiteAmortissable,
        Double mensualiteInFine,
        Double mensualiteGrace,

        Boolean graceActive,
        Integer dureeGraceMois,
        Integer dureeEffectiveMois,

        Double probabiliteDefaut,
        Double var95,
        Double cvar95,
        Double scoreCredit,
        String niveauRisque,

        Double dti,
        Double paymentToIncome,
        Double lti,
        Boolean financialStress,

        Double coutTotalAmortissable,
        Double coutTotalInFine,

        String commentaireAdmin,
        String choisiePar,
        LocalDateTime dateChoix,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
