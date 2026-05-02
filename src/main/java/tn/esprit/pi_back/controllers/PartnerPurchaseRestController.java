package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.partnership.PartnerPurchaseDTO;
import tn.esprit.pi_back.dto.partnership.CreatePurchaseRequest;
import tn.esprit.pi_back.services.PartnerPurchaseService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/partnership/purchases")
@CrossOrigin("*")
public class PartnerPurchaseRestController {

    private final PartnerPurchaseService purchaseService;

    @PostMapping("/create")
    public PartnerPurchaseDTO create(@RequestBody CreatePurchaseRequest req) {
        return purchaseService.createPurchase(req);
    }

    @GetMapping("/client/{clientId}")
    public List<PartnerPurchaseDTO> getByClient(@PathVariable Long clientId) {
        return purchaseService.getByClient(clientId);
    }

    @GetMapping("/all")
    public List<PartnerPurchaseDTO> getAll() {
        return purchaseService.getAll();
    }
}
