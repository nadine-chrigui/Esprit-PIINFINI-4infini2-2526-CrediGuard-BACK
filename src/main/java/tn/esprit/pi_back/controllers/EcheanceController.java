package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.echeance.*;
import tn.esprit.pi_back.services.EcheanceService;

import java.util.List;

@RestController
@RequestMapping("/echeances")
@RequiredArgsConstructor
public class EcheanceController {

    private final EcheanceService service;

    @GetMapping
    public List<EcheanceResponseDTO> getByCredit(@RequestParam Long creditId) {
        return service.getByCredit(creditId);
    }

    @PatchMapping("/{id}/pay")
    public EcheanceResponseDTO pay(
            @PathVariable Long id,
            @Valid @RequestBody EcheancePaymentDTO dto
    ) {
        return service.pay(id, dto);
    }
}