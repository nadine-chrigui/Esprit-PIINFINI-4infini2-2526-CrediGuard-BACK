package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.pi_back.dto.profil.ProfilCreditRequestDTO;
import tn.esprit.pi_back.dto.profil.ProfilCreditResponseDTO;
import tn.esprit.pi_back.services.ProfilCreditService;

import java.util.List;

@RestController
@RequestMapping("/profils-credit")
@RequiredArgsConstructor
public class ProfilCreditController {

    private final ProfilCreditService profilCreditService;

    @GetMapping("/me")
    public ProfilCreditResponseDTO getMyProfile(Authentication authentication) {
        return profilCreditService.getMyProfile(requireEmail(authentication));
    }

    @PostMapping("/me")
    public ProfilCreditResponseDTO createMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfilCreditRequestDTO dto
    ) {
        return profilCreditService.createMyProfile(requireEmail(authentication), dto);
    }

    @PutMapping("/me")
    public ProfilCreditResponseDTO updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfilCreditRequestDTO dto
    ) {
        return profilCreditService.updateMyProfile(requireEmail(authentication), dto);
    }

    @GetMapping("/{id}")
    public ProfilCreditResponseDTO getById(@PathVariable Long id) {
        return profilCreditService.getById(id);
    }

    @GetMapping("/by-client")
    public ProfilCreditResponseDTO getByClientId(@RequestParam Long clientId) {
        return profilCreditService.getByClientId(clientId);
    }

    @GetMapping
    public List<ProfilCreditResponseDTO> getAll(
            @RequestParam(required = false) Long clientId
    ) {
        return profilCreditService.getAll(clientId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        profilCreditService.delete(id);
    }

    private String requireEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
