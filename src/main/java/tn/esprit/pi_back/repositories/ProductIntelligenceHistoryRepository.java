package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.ProductIntelligenceHistory;

import java.util.List;

public interface ProductIntelligenceHistoryRepository extends JpaRepository<ProductIntelligenceHistory, Long> {
    List<ProductIntelligenceHistory> findTop12ByProductIdOrderByAnalyzedAtDesc(Long productId);
}
