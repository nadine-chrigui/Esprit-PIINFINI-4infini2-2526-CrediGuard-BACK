package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.echeance.EcheancePaymentDTO;
import tn.esprit.pi_back.dto.echeance.EcheanceResponseDTO;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.Echeance;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.entities.enums.StatutEcheance;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.mappers.EcheanceMapper;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.EcheanceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EcheanceServiceImpl implements EcheanceService {

    private final EcheanceRepository echeanceRepo;
    private final CreditRepository creditRepo;
    private final EcheanceMapper echeanceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EcheanceResponseDTO> getByCredit(Long creditId) {

        return echeanceRepo.findByCreditId(creditId)
                .stream()
                .map(echeanceMapper::toResponse)
                .toList();
    }

    @Override
    public EcheanceResponseDTO pay(Long echeanceId, EcheancePaymentDTO dto) {

        Echeance echeance = echeanceRepo.findById(echeanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Echeance not found"));

        if (echeance.getStatut() == StatutEcheance.PAYEE) {
            throw new IllegalStateException("Echeance already paid");
        }

        Credit credit = echeance.getCredit();

        if (credit.getStatut() != StatutCredit.ACTIF) {
            throw new IllegalStateException("Credit is not active");
        }

        double totalDu = echeance.getCapitalDu() + echeance.getInteretDu();

        if (dto.montantPaye() < totalDu) {
            throw new IllegalStateException("Payment is insufficient");
        }

        echeance.setStatut(StatutEcheance.PAYEE);

        credit.setMontantRestant(credit.getMontantRestant() - echeance.getCapitalDu());

        if (credit.getMontantRestant() <= 0) {
            credit.setStatut(StatutCredit.CLOTURE);
        }

        return echeanceMapper.toResponse(echeance);
    }
}