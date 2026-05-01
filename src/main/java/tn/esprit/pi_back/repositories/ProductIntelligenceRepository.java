package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pi_back.entities.ProductIntelligence;

import java.util.Optional;

public interface ProductIntelligenceRepository extends JpaRepository<ProductIntelligence, Long> {
    Optional<ProductIntelligence> findByProductId(Long productId);
}
