package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Vente;
import tn.esprit.pi_back.repositories.VenteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenteServiceImpl implements VenteService {

    private final VenteRepository venteRepository;

    @Override
    public Vente create(Vente vente) {
        return venteRepository.save(vente);
    }

    @Override
    public Vente update(Long id, Vente vente) {
        Vente existing = getById(id);
        existing.setMontantTotal(vente.getMontantTotal());
        existing.setDateVente(vente.getDateVente());
        return venteRepository.save(existing);
    }

    @Override
    public Vente getById(Long id) {
        return venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente not found with id: " + id));
    }

    @Override
    public List<Vente> getAll() {
        return venteRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        venteRepository.deleteById(id);
    }
}
