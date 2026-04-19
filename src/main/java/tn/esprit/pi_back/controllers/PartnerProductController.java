package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.PartnerProduct;
import tn.esprit.pi_back.services.IPartnerProductService;

import java.util.List;

@RestController
@RequestMapping("/partner-products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerProductController {

    private final IPartnerProductService service;

    @GetMapping("/partner/{id}")
    public List<PartnerProduct> getByPartner(@PathVariable Long id) {
        return service.getByPartner(id);
    }
    @PostMapping
    public PartnerProduct create(@RequestBody PartnerProduct product) {
        return service.save(product);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @GetMapping("/all")
    public List<PartnerProduct> getAll() {
        return service.getAll();
    }
    @PutMapping("/{id}")
    public PartnerProduct update(@PathVariable Long id, @RequestBody PartnerProduct product) {
        product.setId(id); // 🔥 important
        return service.save(product);
    }


}