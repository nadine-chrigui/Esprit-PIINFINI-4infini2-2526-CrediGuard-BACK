package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.PerformanceTracking;

import java.util.List;

public interface PerformanceTrackingRepository extends JpaRepository<PerformanceTracking, Long> {
    List<PerformanceTracking> findByInvestmentOfferIdOrderByDateAsc(Long offerId);
}
