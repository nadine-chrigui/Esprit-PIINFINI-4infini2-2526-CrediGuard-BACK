package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.CrowdfundingProject;

import java.util.List;

@Repository
public interface CrowdfundingProjectRepository extends JpaRepository<CrowdfundingProject, Long> {
    List<CrowdfundingProject> findByOwnerId(Long ownerId);
    List<CrowdfundingProject> findByStatus(CrowdfundingProject.ProjectStatus status);
}
