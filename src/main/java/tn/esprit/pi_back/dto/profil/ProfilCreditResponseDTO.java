package tn.esprit.pi_back.dto.profil;

import tn.esprit.pi_back.entities.enums.ProfilDefaultFlag;
import tn.esprit.pi_back.entities.enums.ProfilHomeOwnership;
import tn.esprit.pi_back.entities.enums.ProfilLoanIntent;

import java.time.LocalDateTime;

public record ProfilCreditResponseDTO(

        Long id,
        Integer personAge,
        Double personIncomeAnnual,
        ProfilHomeOwnership personHomeOwnership,
        Double personEmploymentLength,
        ProfilDefaultFlag previousDefaultOnFile,
        Integer creditHistoryLength,
        ProfilLoanIntent loanIntent,
        Double monthlyFixedCharges,
        Double existingLoanMonthlyPayments,
        Double outstandingOldDebt,
        Integer projectStartDelayMonths,
        Double expectedMonthlyRevenueAfterStart,
        Boolean hasExistingClients,
        Boolean needsGracePeriod,
        Long clientId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
