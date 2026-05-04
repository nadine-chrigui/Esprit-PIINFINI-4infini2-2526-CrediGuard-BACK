package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Projection.ProjectProjectionRequest;
import tn.esprit.pi_back.dto.Projection.ProjectProjectionResponse;

import java.util.List;

public interface ProjectFinancialProjectionService {
    ProjectProjectionResponse createProjection(ProjectProjectionRequest request);
    List<ProjectProjectionResponse> getByProject(Long projectId);
}
