package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.Crowdfunding.*;
import tn.esprit.pi_back.services.CrowdfundingProjectService;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CrowdfundingProjectController {

    private final CrowdfundingProjectService service;

    @PostMapping
    public ResponseEntity<CrowdfundingResponse> create(@RequestBody CrowdfundingCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public List<CrowdfundingResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CrowdfundingResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
    @GetMapping("/test")
    public String test() {
        return "OK";
    }
    @PutMapping("/{id}")
    public CrowdfundingResponse update(@PathVariable Long id,
                                       @RequestBody CrowdfundingUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}