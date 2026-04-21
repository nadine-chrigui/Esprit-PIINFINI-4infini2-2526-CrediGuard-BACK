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
import tn.esprit.pi_back.mappers.DemandeCreditMapper;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.ProfilCreditRepository;
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
    private final ProfilCreditRepository profilCreditRepository;
    private final DemandeCreditMapper demandeCreditMapper;

    @Override
    public DemandeCreditResponseDTO create(String email, DemandeCreditRequestDTO dto) {
        User client = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!profilCreditRepository.existsByClientId(client.getId())) {
            throw new IllegalStateException("Client must complete credit profile before submitting a credit request");
        }

        DemandeCredit d = new DemandeCredit();
        d.setClient(client);
        d.setTypeCredit(dto.typeCredit());
        d.setMontantDemande(dto.montantDemande());
        d.setDureeMois(dto.dureeMois());
        d.setObjetCredit(dto.objetCredit());
        d.setStatut(StatutDemande.SOUMISE);
        d.setReference(generateUniqueReference());

        DemandeCredit saved = demandeRepo.save(d);
        return demandeCreditMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeCreditResponseDTO getById(Long id) {
        return demandeCreditMapper.toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeCreditResponseDTO> getAll(Long clientId, StatutDemande statut) {
        if (clientId != null && statut != null) {
            return demandeRepo.findByClientIdAndStatut(clientId, statut)
                    .stream()
                    .map(demandeCreditMapper::toResponse)
                    .toList();
        }

        if (clientId != null) {
            return demandeRepo.findByClientId(clientId)
                    .stream()
                    .map(demandeCreditMapper::toResponse)
                    .toList();
        }

        if (statut != null) {
            return demandeRepo.findByStatut(statut)
                    .stream()
                    .map(demandeCreditMapper::toResponse)
                    .toList();
        }

        return demandeRepo.findAll()
                .stream()
                .map(demandeCreditMapper::toResponse)
                .toList();
    }

    @Override
    public DemandeCreditResponseDTO update(Long id, DemandeCreditRequestDTO dto) {
        DemandeCredit d = findEntity(id);

        if (d.getStatut() == StatutDemande.APPROUVEE || d.getStatut() == StatutDemande.REJETEE) {
            throw new IllegalStateException("Cannot update demandeCredit after final decision");
        }

        d.setTypeCredit(dto.typeCredit());
        d.setMontantDemande(dto.montantDemande());
        d.setDureeMois(dto.dureeMois());
        d.setObjetCredit(dto.objetCredit());

        DemandeCredit updated = demandeRepo.save(d);
        return demandeCreditMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        DemandeCredit d = findEntity(id);

        if (d.getStatut() != StatutDemande.SOUMISE) {
            throw new IllegalStateException("Delete allowed only when statut = SOUMISE");
        }

        demandeRepo.delete(d);
    }

    @Override
    public DemandeCreditResponseDTO setStatus(Long id, StatutDemande newStatus) {
        DemandeCredit d = findEntity(id);

        if (d.getStatut() == StatutDemande.APPROUVEE || d.getStatut() == StatutDemande.REJETEE) {
            throw new IllegalStateException("Final status cannot be changed");
        }

        if (d.getStatut() == StatutDemande.SOUMISE && newStatus != StatutDemande.EN_COURS_D_ETUDE) {
            throw new IllegalStateException("From SOUMISE you can only go to EN_COURS_D_ETUDE");
        }

        if (d.getStatut() == StatutDemande.EN_COURS_D_ETUDE
                && !(newStatus == StatutDemande.APPROUVEE || newStatus == StatutDemande.REJETEE)) {
            throw new IllegalStateException("From EN_COURS_D_ETUDE you can only go to APPROUVEE or REJETEE");
        }

        d.setStatut(newStatus);
        DemandeCredit updated = demandeRepo.save(d);
        return demandeCreditMapper.toResponse(updated);
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
}
