package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.ProjectSuccessPrediction;

import java.util.Optional;

@Repository
public interface ProjectSuccessPredictionRepository extends JpaRepository<ProjectSuccessPrediction, Long> {
    Optional<ProjectSuccessPrediction> findTopByProjectProjectIdOrderByGeneratedAtDesc(Long projectId);
}
