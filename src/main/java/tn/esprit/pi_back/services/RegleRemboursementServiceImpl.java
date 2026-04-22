package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.RegleRemboursement;
import tn.esprit.pi_back.repositories.RegleRemboursementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegleRemboursementServiceImpl implements RegleRemboursementService {

    private final RegleRemboursementRepository regleRemboursementRepository;

    @Override
    public RegleRemboursement create(RegleRemboursement regle) {
        return regleRemboursementRepository.save(regle);
    }

    @Override
    public RegleRemboursement update(Long id, RegleRemboursement regle) {
        RegleRemboursement existing = getById(id);
        existing.setTypeRegle(regle.getTypeRegle());
        existing.setValeur(regle.getValeur());
        existing.setCredit(regle.getCredit());
        return regleRemboursementRepository.save(existing);
    }

    @Override
    public RegleRemboursement getById(Long id) {
        return regleRemboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RegleRemboursement not found with id: " + id));
    }

    @Override
    public List<RegleRemboursement> getAll() {
        return regleRemboursementRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        regleRemboursementRepository.deleteById(id);
    }
}
