package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Remboursement;
import tn.esprit.pi_back.repositories.RemboursementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemboursementServiceImpl implements RemboursementService {

    private final RemboursementRepository remboursementRepository;

    @Override
    public Remboursement create(Remboursement remboursement) {
        return remboursementRepository.save(remboursement);
    }

    @Override
    public Remboursement update(Long id, Remboursement remboursement) {
        Remboursement existing = getById(id);
        existing.setMontant(remboursement.getMontant());
        existing.setDateRemboursement(remboursement.getDateRemboursement());
        existing.setMode(remboursement.getMode());
        existing.setCredit(remboursement.getCredit());
        existing.setTransaction(remboursement.getTransaction());
        return remboursementRepository.save(existing);
    }

    @Override
    public Remboursement getById(Long id) {
        return remboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Remboursement not found with id: " + id));
    }

    @Override
    public List<Remboursement> getAll() {
        return remboursementRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        remboursementRepository.deleteById(id);
    }
}
