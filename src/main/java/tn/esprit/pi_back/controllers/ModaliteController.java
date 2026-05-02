package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.evaluation.LigneAmortissementDTO;
import tn.esprit.pi_back.dto.evaluation.ModaliteRequestDTO;
import tn.esprit.pi_back.dto.evaluation.ModaliteResponseDTO;
import tn.esprit.pi_back.services.ModaliteAmortissementService;
import tn.esprit.pi_back.services.ModalitePdfService;
import tn.esprit.pi_back.services.ModaliteService;

import java.util.List;

@RestController
@RequestMapping("/modalites")
@RequiredArgsConstructor
public class ModaliteController {

    private final ModaliteService service;
    private final ModaliteAmortissementService amortissementService;
    private final ModalitePdfService pdfService;

    @PostMapping("/generate")
    public ModaliteResponseDTO generate(@RequestParam Long demandeId) {
        return service.generate(demandeId);
    }

    @PutMapping("/choose")
    public ModaliteResponseDTO choose(
            @RequestParam Long demandeId,
            @Valid @RequestBody ModaliteRequestDTO dto
    ) {
        return service.choose(demandeId, dto);
    }

    @GetMapping
    public ModaliteResponseDTO getByDemande(@RequestParam Long demandeId) {
        return service.getByDemande(demandeId);
    }

    @GetMapping("/amortissement")
    public List<LigneAmortissementDTO> getAmortissement(@RequestParam Long demandeId) {
        return amortissementService.getTableau(demandeId);
    }

    @GetMapping(value = "/amortissement/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAmortissementPdf(@RequestParam Long demandeId) {
        byte[] pdf = pdfService.generatePdf(demandeId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=tableau-amortissement-demande-" + demandeId + ".pdf"
                )
                .body(pdf);
    }
}
