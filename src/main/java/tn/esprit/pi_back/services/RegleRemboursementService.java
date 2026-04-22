package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.RegleRemboursement;
import java.util.List;

public interface RegleRemboursementService {
    RegleRemboursement create(RegleRemboursement regle);

    RegleRemboursement update(Long id, RegleRemboursement regle);

    RegleRemboursement getById(Long id);

    List<RegleRemboursement> getAll();

    void delete(Long id);
}
