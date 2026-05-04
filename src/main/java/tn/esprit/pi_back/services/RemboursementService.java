package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.Remboursement;
import java.util.List;

public interface RemboursementService {
    Remboursement create(Remboursement remboursement);

    Remboursement update(Long id, Remboursement remboursement);

    Remboursement getById(Long id);

    List<Remboursement> getAll();

    void delete(Long id);
}
