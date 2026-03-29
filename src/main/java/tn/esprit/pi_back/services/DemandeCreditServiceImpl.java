package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.pi_back.dto.demande.DemandeCreditRequestDTO;
import tn.esprit.pi_back.dto.demande.DemandeCreditResponseDTO;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.exceptions.ResourceNotFoundException;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeCreditServiceImpl implements DemandeCreditService {

    private final DemandeCreditRepository demandeRepo;
    private final UserRepository userRepo;

    @Override
    public DemandeCreditResponseDTO create(String email, DemandeCreditRequestDTO dto) {
        User client = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        DemandeCredit d = new DemandeCredit();
        d.setClient(client);
        d.setTypeCredit(dto.typeCredit());
        d.setMontantDemande(dto.montantDemande());
        d.setDureeMois(dto.dureeMois());
        d.setObjetCredit(dto.objetCredit());
        d.setStatut(StatutDemande.SOUMISE);
        d.setReference(generateUniqueReference());

        DemandeCredit saved = demandeRepo.save(d);
        return toDTO(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public DemandeCreditResponseDTO getById(Long id) {
        return toDTO(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeCreditResponseDTO> getAll(Long clientId, StatutDemande statut) {
        if (clientId != null && statut != null) {
            return demandeRepo.findByClientIdAndStatut(clientId, statut).stream().map(this::toDTO).toList();
        }
        if (clientId != null) {
            return demandeRepo.findByClientId(clientId).stream().map(this::toDTO).toList();
        }
        if (statut != null) {
            return demandeRepo.findByStatut(statut).stream().map(this::toDTO).toList();
        }
        return demandeRepo.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public DemandeCreditResponseDTO update(Long id, DemandeCreditRequestDTO dto) {
        DemandeCredit d = findEntity(id);

        // Règle métier simple : si déjà APPROUVEE/REJETEE, on ne modifie plus
        if (d.getStatut() == StatutDemande.APPROUVEE || d.getStatut() == StatutDemande.REJETEE) {
            throw new IllegalStateException("Cannot update demandeCredit after final decision");
        }

        d.setTypeCredit(dto.typeCredit());
        d.setMontantDemande(dto.montantDemande());
        d.setDureeMois(dto.dureeMois());
        d.setObjetCredit(dto.objetCredit());

        return toDTO(demandeRepo.save(d));
    }

    @Override
    public void delete(Long id) {
        DemandeCredit d = findEntity(id);

        // Banque: généralement on ne supprime pas si dossier en cours/final.
        // Ici: autoriser suppression seulement si SOUMISE (à toi de choisir).
        if (d.getStatut() != StatutDemande.SOUMISE) {
            throw new IllegalStateException("Delete allowed only when statut = SOUMISE");
        }

        demandeRepo.delete(d);
    }

    @Override
    public DemandeCreditResponseDTO setStatus(Long id, StatutDemande newStatus) {
        DemandeCredit d = findEntity(id);

        // garde-fou workflow
        if (d.getStatut() == StatutDemande.APPROUVEE || d.getStatut() == StatutDemande.REJETEE) {
            throw new IllegalStateException("Final status cannot be changed");
        }

        // Exemple de transitions autorisées:
        // SOUMISE -> EN_COURS_D_ETUDE
        // EN_COURS_D_ETUDE -> APPROUVEE/REJETEE
        if (d.getStatut() == StatutDemande.SOUMISE && newStatus != StatutDemande.EN_COURS_D_ETUDE) {
            throw new IllegalStateException("From SOUMISE you can only go to EN_COURS_D_ETUDE");
        }
        if (d.getStatut() == StatutDemande.EN_COURS_D_ETUDE
                && !(newStatus == StatutDemande.APPROUVEE || newStatus == StatutDemande.REJETEE)) {
            throw new IllegalStateException("From EN_COURS_D_ETUDE you can only go to APPROUVEE or REJETEE");
        }

        d.setStatut(newStatus);
        return toDTO(demandeRepo.save(d));
    }

    private DemandeCredit findEntity(Long id) {
        return demandeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeCredit not found: " + id));
    }

    private String generateUniqueReference() {
        String ref;
        do {
            ref = "DMD-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (demandeRepo.existsByReference(ref));
        return ref;
    }

    private DemandeCreditResponseDTO toDTO(DemandeCredit d) {
        Long voucherId = (d.getVoucher() != null) ? d.getVoucher().getId() : null;
        Long clientId = (d.getClient() != null) ? d.getClient().getId() : null;
        String clientName = (d.getClient() != null) ? d.getClient().getFullName() : null;

        return new DemandeCreditResponseDTO(
                d.getId(),
                d.getReference(),
                d.getTypeCredit(),
                d.getMontantDemande(),
                d.getDureeMois(),
                d.getObjetCredit(),
                d.getStatut(),
                d.getDateCreation(),
                clientId,
                clientName,
                voucherId,
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}