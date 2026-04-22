package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.CompteFinancier;
import tn.esprit.pi_back.repositories.CompteFinancierRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompteFinancierServiceImpl implements CompteFinancierService {

    private final CompteFinancierRepository compteFinancierRepository;

    @Override
    public CompteFinancier create(CompteFinancier compte) {
        return compteFinancierRepository.save(compte);
    }

    @Override
    public CompteFinancier update(Long id, CompteFinancier compte) {
        CompteFinancier existing = getById(id);
        existing.setSolde(compte.getSolde());
        existing.setTypeCompte(compte.getTypeCompte());
        existing.setUtilisateur(compte.getUtilisateur());
        return compteFinancierRepository.save(existing);
    }

    @Override
    public CompteFinancier getById(Long id) {
        return compteFinancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CompteFinancier not found with id: " + id));
    }

    @Override
    public List<CompteFinancier> getAll() {
        return compteFinancierRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        compteFinancierRepository.deleteById(id);
    }
}
