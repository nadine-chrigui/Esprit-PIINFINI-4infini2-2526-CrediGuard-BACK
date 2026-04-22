package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsurancePolicyDTO;
import tn.esprit.pi_back.dto.insurance.InsurancePolicyMapper;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.services.IInsurancePolicyService;
import tn.esprit.pi_back.services.UserServiceImpl;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import tn.esprit.pi_back.entities.User;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance/policies")
@lombok.extern.slf4j.Slf4j
public class InsurancePolicyRestController {

    private final IInsurancePolicyService service;
    private final UserServiceImpl userService;
    // CREATE POLICY
    // =========================
    @PostMapping
    public InsurancePolicyDTO create(@RequestBody InsurancePolicy policy) {

        InsurancePolicy saved = service.addAndAssign(
                policy.getInsuranceCompany().getId(),
                policy.getClient().getId(),
                policy
        );

        if (saved == null)
            throw new ResponseStatusException(NOT_FOUND, "Company or Client not found");

        return InsurancePolicyMapper.toDTO(saved);
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/update")
    public InsurancePolicyDTO update(@RequestBody InsurancePolicy policy) {
        InsurancePolicy updated = service.update(policy);

        if (updated == null)
            throw new ResponseStatusException(NOT_FOUND, "Policy not found");

        return InsurancePolicyMapper.toDTO(updated);
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // =========================
    // GET BY ID
    // =========================
    @GetMapping("/get/{id}")
    public InsurancePolicyDTO get(@PathVariable Long id) {
        InsurancePolicy p = service.get(id);

        if (p == null)
            throw new ResponseStatusException(NOT_FOUND, "Policy not found");

        return InsurancePolicyMapper.toDTO(p);
    }

    // =========================
    // GET ALL
    // =========================
    @GetMapping("/all")
    public List<InsurancePolicyDTO> all() {
        return service.all()
                .stream()
                .map(InsurancePolicyMapper::toDTO)
                .toList();
    }

    // =========================
    // GET MY POLICY (FIX FINAL)
    // =========================
    @GetMapping("/my-policy")
    public InsurancePolicyDTO getMyPolicy() {

        User user = userService.getCurrentUserOrThrow();

        System.out.println("USER CONNECTÉ ID: " + user.getId()); //
        System.out.println("USER EMAIL: " + user.getEmail());
        InsurancePolicy policy = service.getPolicyByUserId(user.getId());

        if (policy == null) {
            throw new ResponseStatusException(NOT_FOUND, "Policy not found");
        }

        return InsurancePolicyMapper.toDTO(policy);
    }
    // =========================
// GET POLICY BY CLIENT ID (DYNAMIQUE)
// =========================
    @GetMapping("/by-client/{clientId}")
    public List<InsurancePolicyDTO> getByClient(@PathVariable Long clientId) {
        log.info("Request to get policies for client: {}", clientId);

        List<InsurancePolicy> policies = service.getPoliciesByUserId(clientId);

        if (policies == null) {
            return java.util.Collections.emptyList();
        }

        return policies.stream()
                .map(InsurancePolicyMapper::toDTO)
                .toList();
    }

}