package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Projection.ProjectProjectionRequest;
import tn.esprit.pi_back.dto.Projection.ProjectProjectionResponse;
import tn.esprit.pi_back.services.ProjectFinancialProjectionService;

import java.util.List;

@RestController
@RequestMapping("/project-projections")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProjectFinancialProjectionController {

    private final ProjectFinancialProjectionService projectFinancialProjectionService;

    @PostMapping
    public ResponseEntity<ProjectProjectionResponse> create(@Valid @RequestBody ProjectProjectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectFinancialProjectionService.createProjection(request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectProjectionResponse>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFinancialProjectionService.getByProject(projectId));
    }
}
