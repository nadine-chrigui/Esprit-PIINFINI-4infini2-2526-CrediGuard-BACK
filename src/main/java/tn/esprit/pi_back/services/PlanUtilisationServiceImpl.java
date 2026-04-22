package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.plan.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanUtilisationServiceImpl implements PlanUtilisationService {

    private final PlanUtilisationRepository planRepo;
    private final DemandeCreditRepository demandeRepo;

    @Override
    public PlanUtilisationResponseDTO create(Long demandeId, PlanUtilisationRequestDTO dto) {

        DemandeCredit demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        if (planRepo.existsByDemandeCreditId(demandeId))
            throw new IllegalStateException("Plan already exists for this demande");

        if (demande.getStatut() == StatutDemande.APPROUVEE ||
                demande.getStatut() == StatutDemande.REJETEE)
            throw new IllegalStateException("Cannot create plan after final decision");

        PlanUtilisationCredit plan = new PlanUtilisationCredit();

        plan.setDescriptionProjet(dto.descriptionProjet());
        plan.setObjectifCredit(dto.objectifCredit());
        plan.setMontantInvestissement(dto.montantInvestissement());
        plan.setRevenuMensuelPrevu(dto.revenuMensuelPrevu());
        plan.setProfitMensuelPrevu(dto.profitMensuelPrevu());
        plan.setDelaiRentabiliteMois(dto.delaiRentabiliteMois());
        plan.setNatureActivite(dto.natureActivite());
        plan.setDemandeCredit(demande);

        PlanUtilisationCredit saved = planRepo.save(plan);

        return toDTO(saved);
    }

    @Override
    public PlanUtilisationResponseDTO getByDemande(Long demandeId) {

        PlanUtilisationCredit plan = planRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        return toDTO(plan);
    }

    @Override
    public PlanUtilisationResponseDTO update(Long demandeId, PlanUtilisationRequestDTO dto) {

        PlanUtilisationCredit plan = planRepo.findByDemandeCreditId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        plan.setDescriptionProjet(dto.descriptionProjet());
        plan.setObjectifCredit(dto.objectifCredit());
        plan.setMontantInvestissement(dto.montantInvestissement());
        plan.setRevenuMensuelPrevu(dto.revenuMensuelPrevu());
        plan.setProfitMensuelPrevu(dto.profitMensuelPrevu());
        plan.setDelaiRentabiliteMois(dto.delaiRentabiliteMois());
        plan.setNatureActivite(dto.natureActivite());

        return toDTO(plan);
    }

    private PlanUtilisationResponseDTO toDTO(PlanUtilisationCredit p) {

        return new PlanUtilisationResponseDTO(
                p.getId(),
                p.getDescriptionProjet(),
                p.getObjectifCredit(),
                p.getMontantInvestissement(),
                p.getRevenuMensuelPrevu(),
                p.getProfitMensuelPrevu(),
                p.getDelaiRentabiliteMois(),
                p.getNatureActivite(),
                p.getDemandeCredit().getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanUtilisationResponseDTO> getMine(Long clientId) {
        return planRepo.findAllByDemandeCreditClientId(clientId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
}