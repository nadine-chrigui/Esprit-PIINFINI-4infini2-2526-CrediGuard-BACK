package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.echeance.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.entities.enums.StatutEcheance;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EcheanceServiceImpl implements EcheanceService {

    private final EcheanceRepository echeanceRepo;
    private final CreditRepository creditRepo;

    @Override
    public List<EcheanceResponseDTO> getByCredit(Long creditId) {

        return echeanceRepo.findByCreditId(creditId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public EcheanceResponseDTO pay(Long echeanceId, EcheancePaymentDTO dto) {

        Echeance echeance = echeanceRepo.findById(echeanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Echeance not found"));

        if (echeance.getStatut() == StatutEcheance.PAYEE)
            throw new IllegalStateException("Echeance already paid");

        Credit credit = echeance.getCredit();

        if (credit.getStatut() != StatutCredit.ACTIF)
            throw new IllegalStateException("Credit is not active");

        double totalDu = echeance.getCapitalDu() + echeance.getInteretDu();

        if (dto.montantPaye() < totalDu)
            throw new IllegalStateException("Payment is insufficient");

        // Marquer échéance payée
        echeance.setStatut(StatutEcheance.PAYEE);

        // Réduire montant restant du credit
        credit.setMontantRestant(credit.getMontantRestant() - echeance.getCapitalDu());

        // Si tout payé → clôture
        if (credit.getMontantRestant() <= 0)
            credit.setStatut(StatutCredit.CLOTURE);

        return toDTO(echeance);
    }

    private EcheanceResponseDTO toDTO(Echeance e) {
        return new EcheanceResponseDTO(
                e.getId(),
                e.getDateEcheance(),
                e.getCapitalDu(),
                e.getInteretDu(),
                e.getStatut(),
                e.getCredit().getId()
        );
    }
}