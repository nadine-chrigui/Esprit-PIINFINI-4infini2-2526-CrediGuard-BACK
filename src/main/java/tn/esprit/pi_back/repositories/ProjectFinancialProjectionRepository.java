package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.ProjectFinancialProjection;

import java.util.List;

@Repository
public interface ProjectFinancialProjectionRepository extends JpaRepository<ProjectFinancialProjection, Long> {
    List<ProjectFinancialProjection> findByProjectProjectIdOrderByCreatedAtDesc(Long projectId);
}
