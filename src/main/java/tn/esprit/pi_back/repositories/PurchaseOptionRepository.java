package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.PurchaseOption;

import java.util.List;

@Repository
public interface PurchaseOptionRepository extends JpaRepository<PurchaseOption, Long> {
    List<PurchaseOption> findByProjectProjectId(Long projectId);
    List<PurchaseOption> findByStatus(PurchaseOption.OptionStatus status);
}
