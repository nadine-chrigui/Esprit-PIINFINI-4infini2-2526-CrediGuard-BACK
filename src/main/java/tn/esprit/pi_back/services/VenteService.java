package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.Vente;
import java.util.List;

public interface VenteService {
    Vente create(Vente vente);

    Vente update(Long id, Vente vente);

    Vente getById(Long id);

    List<Vente> getAll();

    void delete(Long id);
}
