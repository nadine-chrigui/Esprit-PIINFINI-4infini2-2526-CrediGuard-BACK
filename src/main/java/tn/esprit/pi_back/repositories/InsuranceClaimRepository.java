package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.pi_back.entities.InsuranceClaim;

import java.time.LocalDateTime;
import java.util.List;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    List<InsuranceClaim> findByUserId(Long userId);

    @Query("SELECT COUNT(c) FROM InsuranceClaim c WHERE c.user.id = :userId AND c.declaredAt >= :since")
    long countByUserIdAndDeclaredAtAfter(@Param("userId") Long userId,
                                          @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(c) FROM InsuranceClaim c WHERE c.insurancePolicy.client.id = :clientId AND c.declaredAt >= :since")
    long countByPolicyClientIdAndDeclaredAtAfter(@Param("clientId") Long clientId,
                                                  @Param("since") LocalDateTime since);

    boolean existsByVoucherId(Long voucherId);
}