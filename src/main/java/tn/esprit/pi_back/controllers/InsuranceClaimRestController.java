package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimDTO;
import tn.esprit.pi_back.dto.insurance.InsuranceClaimMapper;
import tn.esprit.pi_back.dto.insurance.CreateClaimRequest;
import tn.esprit.pi_back.services.IInsuranceClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance/claims")
public class InsuranceClaimRestController {

    private final IInsuranceClaimService service;

    @PostMapping("/create")
    public InsuranceClaimDTO create(@RequestBody CreateClaimRequest request) {

        return InsuranceClaimMapper.toDTO(
                service.createClaim(
                        request.getVoucherId(),
                        request.getPolicyId(),
                        request.getClaimReference()
                )
        );
    }

    @PutMapping("/approve/{idClaim}")
    public InsuranceClaimDTO approve(@PathVariable Long idClaim) {
        return InsuranceClaimMapper.toDTO(service.approve(idClaim));
    }

    @PutMapping("/reject/{idClaim}")
    public InsuranceClaimDTO reject(@PathVariable Long idClaim,
                                    @RequestParam String reason) {
        return InsuranceClaimMapper.toDTO(service.reject(idClaim, reason));
    }

    @PutMapping("/update")
    public InsuranceClaimDTO update(@RequestBody InsuranceClaimDTO dto) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Use approve/reject instead");
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/get/{id}")
    public InsuranceClaimDTO get(@PathVariable Long id) {
        var c = service.get(id);
        if (c == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found");
        return InsuranceClaimMapper.toDTO(c);
    }

    @GetMapping("/all")
    public List<InsuranceClaimDTO> all() {
        return service.all()
                .stream()
                .map(InsuranceClaimMapper::toDTO)
                .toList();
    }
}