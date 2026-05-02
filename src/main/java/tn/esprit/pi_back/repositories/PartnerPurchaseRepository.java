package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.PartnerPurchase;
import java.util.List;

@Repository
public interface PartnerPurchaseRepository extends JpaRepository<PartnerPurchase, Long> {
    List<PartnerPurchase> findByClientId(Long clientId);
}
