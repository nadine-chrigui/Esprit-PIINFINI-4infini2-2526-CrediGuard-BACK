package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.FinancialScore;
import java.util.Optional;

@Repository
public interface FinancialScoreRepository extends JpaRepository<FinancialScore, Long> {
    Optional<FinancialScore> findByUserId(Long userId);
}