package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.DeliveryZone;

import java.util.List;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    List<DeliveryZone> findByActiveTrueOrderByRiskLevelDesc();
    List<DeliveryZone> findByActiveTrueOrderByNameAsc();
}
