package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Carte;
import tn.esprit.pi_back.repositories.CarteRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarteService {

    private final CarteRepository carteRepository;

    public List<Carte> getAllCartes() {
        return carteRepository.findAll();
    }

    public Optional<Carte> getCarteById(Long id) {
        return carteRepository.findById(id);
    }

    public Carte createCarte(Carte carte) {
        if (carte.getId() != null && carte.getId() == 0) {
            carte.setId(null);
        }
        return carteRepository.save(carte);
    }

    public Carte updateCarte(Long id, Carte updatedCarte) {
        return carteRepository.findById(id)
                .map(existingCarte -> {
                    existingCarte.setCardNumber(updatedCarte.getCardNumber());
                    existingCarte.setCardHolderName(updatedCarte.getCardHolderName());
                    existingCarte.setCvv(updatedCarte.getCvv());
                    existingCarte.setExpiryDate(updatedCarte.getExpiryDate());
                    existingCarte.setActive(updatedCarte.isActive());
                    if (updatedCarte.getCompte() != null) {
                        existingCarte.setCompte(updatedCarte.getCompte());
                    }
                    if (updatedCarte.getLoyaltyLevel() != null) {
                        existingCarte.setLoyaltyLevel(updatedCarte.getLoyaltyLevel());
                    }
                    return carteRepository.save(existingCarte);
                })
                .orElseThrow(() -> new IllegalArgumentException("Carte non trouvée avec l'ID: " + id));
    }

    public void deleteCarte(Long id) {
        carteRepository.deleteById(id);
    }
}