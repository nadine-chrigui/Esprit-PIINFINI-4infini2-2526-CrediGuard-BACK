package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.credit.CreditRequestDTO;
import tn.esprit.pi_back.dto.credit.CreditResponseDTO;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.Echeance;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.entities.enums.StatutEcheance;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.EcheanceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepo;
    private final DemandeCreditRepository demandeRepo;
    private final EcheanceRepository echeanceRepo;

    @Override
    public CreditResponseDTO create(Long demandeId, CreditRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (creditRepo.existsByDemandeCreditId(demandeId)) {
            throw new IllegalStateException("Credit already exists for this demande");
        }

        if (demande.getStatut() != StatutDemande.APPROUVEE) {
            throw new IllegalStateException("Credit can only be created if demande is APPROUVEE");
        }

        if (demande.getDureeMois() == null || demande.getDureeMois() <= 0) {
            throw new IllegalStateException("Demande duration must be greater than 0");
        }

        Credit credit = new Credit();

        credit.setDemandeCredit(demande);
        credit.setClient(demande.getClient());

        credit.setMontantAccorde(dto.montantAccorde());
        credit.setMontantTotal(dto.montantAccorde());
        credit.setMontantRestant(dto.montantAccorde());

        credit.setTauxRemboursement(dto.tauxRemboursement());
        credit.setModeRemboursement(dto.modeRemboursement());

        if (credit.getDateDebut() == null) {
            credit.setDateDebut(LocalDateTime.now());
        }

        credit.setDateFin(dto.dateFin());
        credit.setStatut(StatutCredit.ACTIF);

        Credit saved = creditRepo.save(credit);

        generateEcheances(saved);

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
        credit.setMontantTotal(dto.montantAccorde());

        if (credit.getMontantRestant() > credit.getMontantTotal()) {
            credit.setMontantRestant(credit.getMontantTotal());
        }

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

    @Override
    public CreditResponseDTO getByDemande(Long demandeId) {
        Credit credit = creditRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit not found"));

        return toDTO(credit);
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

    private void generateEcheances(Credit credit) {
        int dureeMois = credit.getDemandeCredit().getDureeMois();

        double montantTotal = credit.getMontantTotal() != null
                ? credit.getMontantTotal()
                : credit.getMontantAccorde();

        double capitalMensuel = montantTotal / dureeMois;

        for (int i = 1; i <= dureeMois; i++) {
            Echeance echeance = new Echeance();
            echeance.setCredit(credit);
            echeance.setDateEcheance(credit.getDateDebut().plusMonths(i));
            echeance.setCapitalDu(capitalMensuel);
            echeance.setInteretDu(0.0);
            echeance.setStatut(StatutEcheance.A_PAYER);

            echeanceRepo.save(echeance);
        }
    }
}