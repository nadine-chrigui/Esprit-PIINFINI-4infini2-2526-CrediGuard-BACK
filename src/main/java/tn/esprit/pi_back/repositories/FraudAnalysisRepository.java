package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.FraudAnalysisResult;
import java.util.Optional;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysisResult, Long> {
    Optional<FraudAnalysisResult> findByTransactionId(Long transactionId);
}