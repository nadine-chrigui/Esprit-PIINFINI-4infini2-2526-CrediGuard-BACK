package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.RiskScore;
import java.util.Optional;

@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {
    Optional<RiskScore> findByUserId(Long userId);
}
