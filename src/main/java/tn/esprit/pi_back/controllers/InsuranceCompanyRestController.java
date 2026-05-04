package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.InsuranceCompany;
import tn.esprit.pi_back.services.IInsuranceCompanyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance/companies")
public class InsuranceCompanyRestController {

    private final IInsuranceCompanyService service;

    @PostMapping("/add")
    public InsuranceCompany add(@RequestBody InsuranceCompany c) {
        return service.save(c);
    }

    @PutMapping("/update")
    public InsuranceCompany update(@RequestBody InsuranceCompany c) {
        return service.update(c.getId(), c);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/get/{id}")
    public InsuranceCompany get(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/all")
    public List<InsuranceCompany> all() {
        return service.getAll();
    }
}