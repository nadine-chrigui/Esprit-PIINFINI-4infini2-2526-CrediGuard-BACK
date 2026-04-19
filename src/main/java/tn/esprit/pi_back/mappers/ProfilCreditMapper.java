package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.profil.ProfilCreditRequestDTO;
import tn.esprit.pi_back.dto.profil.ProfilCreditResponseDTO;
import tn.esprit.pi_back.entities.ProfilCredit;

@Component
public class ProfilCreditMapper {

    public ProfilCredit toEntity(ProfilCreditRequestDTO dto) {
        ProfilCredit profil = new ProfilCredit();
        updateEntityFromDto(profil, dto);
        return profil;
    }

    public void updateEntityFromDto(ProfilCredit profil, ProfilCreditRequestDTO dto) {
        profil.setPersonAge(dto.personAge());
        profil.setPersonIncomeAnnual(dto.personIncomeAnnual());
        profil.setPersonHomeOwnership(dto.personHomeOwnership());
        profil.setPersonEmploymentLength(dto.personEmploymentLength());
        profil.setPreviousDefaultOnFile(dto.previousDefaultOnFile());
        profil.setCreditHistoryLength(dto.creditHistoryLength());
        profil.setLoanIntent(dto.loanIntent());
        profil.setMonthlyFixedCharges(dto.monthlyFixedCharges());
        profil.setExistingLoanMonthlyPayments(dto.existingLoanMonthlyPayments());
        profil.setOutstandingOldDebt(dto.outstandingOldDebt());
        profil.setProjectStartDelayMonths(dto.projectStartDelayMonths());
        profil.setExpectedMonthlyRevenueAfterStart(dto.expectedMonthlyRevenueAfterStart());
        profil.setHasExistingClients(dto.hasExistingClients());
        profil.setNeedsGracePeriod(dto.needsGracePeriod());
    }

    public ProfilCreditResponseDTO toResponse(ProfilCredit profil) {
        return new ProfilCreditResponseDTO(
                profil.getId(),
                profil.getPersonAge(),
                profil.getPersonIncomeAnnual(),
                profil.getPersonHomeOwnership(),
                profil.getPersonEmploymentLength(),
                profil.getPreviousDefaultOnFile(),
                profil.getCreditHistoryLength(),
                profil.getLoanIntent(),
                profil.getMonthlyFixedCharges(),
                profil.getExistingLoanMonthlyPayments(),
                profil.getOutstandingOldDebt(),
                profil.getProjectStartDelayMonths(),
                profil.getExpectedMonthlyRevenueAfterStart(),
                profil.getHasExistingClients(),
                profil.getNeedsGracePeriod(),
                profil.getClient().getId(),
                profil.getCreatedAt(),
                profil.getUpdatedAt()
        );
    }
}
