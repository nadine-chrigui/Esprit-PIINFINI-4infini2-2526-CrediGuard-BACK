package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.credit.CreditRequestDTO;
import tn.esprit.pi_back.dto.credit.CreditResponseDTO;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepo;
    private final DemandeCreditRepository demandeRepo;

    @Override
    public CreditResponseDTO create(Long demandeId, CreditRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (demande.getStatut() != tn.esprit.pi_back.entities.enums.StatutDemande.APPROUVEE) {
            throw new IllegalStateException("Credit can only be created if demande is APPROUVEE");
        }

        Credit credit = new Credit();

        credit.setDemandeCredit(demande);
        credit.setClient(demande.getClient());

        credit.setMontantAccorde(dto.montantAccorde());
        credit.setTauxRemboursement(dto.tauxRemboursement());
        credit.setModeRemboursement(dto.modeRemboursement());
        credit.setDateFin(dto.dateFin());

        credit.setStatut(StatutCredit.ACTIF);

        Credit saved = creditRepo.save(credit);

        return toDTO(saved);
    }

    @Override
    public CreditResponseDTO getById(Long id) {
        return toDTO(find(id));
    }

    @Override
    public List<CreditResponseDTO> getAll(Long clientId, StatutCredit statut) {
        if (clientId != null && statut != null)
            return creditRepo.findByClientIdAndStatut(clientId, statut).stream().map(this::toDTO).toList();

        if (clientId != null)
            return creditRepo.findByClientId(clientId).stream().map(this::toDTO).toList();

        if (statut != null)
            return creditRepo.findByStatut(statut).stream().map(this::toDTO).toList();

        return creditRepo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public CreditResponseDTO update(Long id, CreditRequestDTO dto) {

        Credit credit = find(id);

        if (credit.getStatut() != StatutCredit.ACTIF)
            throw new IllegalStateException("Only ACTIF credit can be updated");

        credit.setMontantAccorde(dto.montantAccorde());
        credit.setTauxRemboursement(dto.tauxRemboursement());
        credit.setModeRemboursement(dto.modeRemboursement());
        credit.setDateFin(dto.dateFin());

        return toDTO(creditRepo.save(credit));
    }

    @Override
    public void delete(Long id) {
        Credit credit = find(id);

        if (credit.getStatut() == StatutCredit.ACTIF)
            throw new IllegalStateException("Cannot delete active credit");

        creditRepo.delete(credit);
    }

    @Override
    public CreditResponseDTO changeStatus(Long id, StatutCredit statut) {

        Credit credit = find(id);

        credit.setStatut(statut);

        return toDTO(creditRepo.save(credit));
    }

    private Credit find(Long id) {
        return creditRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit not found"));
    }

    private CreditResponseDTO toDTO(Credit c) {
        return new CreditResponseDTO(
                c.getId(),
                c.getMontantAccorde(),
                c.getMontantTotal(),
                c.getMontantRestant(),
                c.getTauxRemboursement(),
                c.getDateDebut(),
                c.getDateFin(),
                c.getStatut(),
                c.getModeRemboursement(),
                c.getClient().getId(),
                c.getDemandeCredit().getId(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}