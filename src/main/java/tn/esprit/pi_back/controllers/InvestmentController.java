package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Investment.*;
import tn.esprit.pi_back.services.InvestmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentResponse> create(@Valid @RequestBody InvestmentCreateRequest req) {
        InvestmentResponse created = investmentService.create(req);
        return ResponseEntity.created(URI.create("/api/investments/" + created.investmentId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<InvestmentResponse>> getAll() {
        return ResponseEntity.ok(investmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(investmentService.getById(id));
    }

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<InvestmentResponse>> getByInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(investmentService.getByInvestor(investorId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<InvestmentResponse>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(investmentService.getByProject(projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponse> update(@PathVariable Long id,
                                                     @RequestBody InvestmentUpdateRequest req) {
        return ResponseEntity.ok(investmentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
