package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsuranceCompanyDTO;
import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.services.IInsuranceCompanyService;

import java.util.List;

@RestController
@RequestMapping("/assureurs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AssureurController {

    private final IInsuranceCompanyService companyService;

    @GetMapping
    public ResponseEntity<List<InsuranceCompanyDTO>> getAll() {
        return ResponseEntity.ok(companyService.getAllPublic());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceCompanyDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getByIdWithOffers(id));
    }

    @PostMapping
    public ResponseEntity<InsuranceCompany> create(@RequestBody InsuranceCompany company) {
        return ResponseEntity.ok(companyService.save(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsuranceCompany> update(@PathVariable Long id, @RequestBody InsuranceCompany company) {
        return ResponseEntity.ok(companyService.update(id, company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
