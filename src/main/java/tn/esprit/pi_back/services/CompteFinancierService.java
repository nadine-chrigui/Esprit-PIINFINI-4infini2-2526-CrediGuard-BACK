package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.CompteFinancier;
import java.util.List;

public interface CompteFinancierService {
    CompteFinancier create(CompteFinancier compte);

    CompteFinancier update(Long id, CompteFinancier compte);

    CompteFinancier getById(Long id);

    List<CompteFinancier> getAll();

    void delete(Long id);
}
