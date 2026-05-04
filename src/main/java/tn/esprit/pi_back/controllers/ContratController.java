package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.InsurancePolicyDTO;
import tn.esprit.pi_back.dto.insurance.InsurancePolicyMapper;
import tn.esprit.pi_back.entities.InsurancePolicy;
import tn.esprit.pi_back.services.IInsurancePolicyService;
import tn.esprit.pi_back.services.InsurancePdfService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/contrats")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ContratController {

    private final IInsurancePolicyService policyService;
    private final InsurancePdfService pdfService;

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long clientId,
                                                      @RequestParam Long offerId,
                                                      @RequestParam Double declaredValue,
                                                      @RequestParam String goodsNature,
                                                      @RequestParam(required = false) String voucherCode,
                                                      @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate) {
        try {
            InsurancePolicy policy = policyService.createContract(clientId, offerId, declaredValue, goodsNature, voucherCode, startDate);
            return ResponseEntity.ok(InsurancePolicyMapper.toDTO(policy));
        } catch (Exception e) {
            log.error("Erreur création contrat: ", e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdfContent = pdfService.generatePolicyPdf(id);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=Contrat_Assurance_" + id + ".pdf")
                .body(pdfContent);
    }

    @GetMapping("/verify/{policyNumber}")
    public ResponseEntity<String> verify(@PathVariable String policyNumber) {
        InsurancePolicy policy = policyService.all().stream()
                .filter(p -> p.getPolicyNumber().equals(policyNumber))
                .findFirst()
                .orElse(null);

        String html;
        if (policy != null) {
            html = """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Vérification CrediGuard</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;700;800&display=swap" rel="stylesheet">
                    <style>
                        body { font-family: 'Inter', sans-serif; background: #f4f7fe; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
                        .card { background: white; padding: 40px; border-radius: 24px; box-shadow: 0 20px 50px rgba(0,0,0,0.1); text-align: center; max-width: 400px; width: 90%%; }
                        .shield { font-size: 60px; color: #2ecc71; margin-bottom: 20px; }
                        h1 { color: #1a237e; font-size: 22px; margin-bottom: 10px; }
                        p { color: #666; font-size: 14px; line-height: 1.6; }
                        .status { display: inline-block; background: #e8f5e9; color: #2e7d32; padding: 8px 20px; border-radius: 50px; font-weight: 800; font-size: 12px; margin: 20px 0; text-transform: uppercase; }
                        .details { text-align: left; background: #f8f9fa; padding: 20px; border-radius: 12px; margin-top: 20px; }
                        .details-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
                        .details-label { color: #999; font-weight: 600; }
                        .details-value { color: #333; font-weight: 700; }
                        .footer { margin-top: 30px; font-size: 11px; color: #aaa; border-top: 1px solid #eee; padding-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="shield">🛡️</div>
                        <h1>Contrat Authentifié</h1>
                        <p>Ce document est certifié par le système de sécurité <strong>CrediGuard</strong>.</p>
                        <div class="status">✅ Authentique & Valide</div>
                        <div class="details">
                            <div class="details-row"><span class="details-label">N° Contrat :</span><span class="details-value">%s</span></div>
                            <div class="details-row"><span class="details-label">Client :</span><span class="details-value">%s</span></div>
                            <div class="details-row"><span class="details-label">Assureur :</span><span class="details-value">%s</span></div>
                            <div class="details-row"><span class="details-label">Validité :</span><span class="details-value">jusqu'au %s</span></div>
                        </div>
                        <div class="footer">CrediGuard Digital Verification System &copy; 2026</div>
                    </div>
                </body>
                </html>
                """.formatted(
                    policy.getPolicyNumber(),
                    policy.getClient() != null ? policy.getClient().getFullName() : "N/A",
                    policy.getInsuranceCompany() != null ? policy.getInsuranceCompany().getName() : "N/A",
                    policy.getEndDate() != null ? policy.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"
                );
        } else {
            html = """
                <body style="font-family:sans-serif; text-align:center; padding-top:100px; background:#fff5f5;">
                    <h1 style="color:red;">❌ Contrat Non Trouvé</h1>
                    <p>Ce numéro de contrat ne correspond à aucun enregistrement valide.</p>
                </body>
                """;
        }

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    @PatchMapping("/{id}/renouveler")
    public ResponseEntity<InsurancePolicyDTO> renew(@PathVariable Long id) {
        InsurancePolicy policy = policyService.renewContract(id);
        return ResponseEntity.ok(InsurancePolicyMapper.toDTO(policy));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InsurancePolicyDTO>> getByClient(@PathVariable Long clientId) {
        List<InsurancePolicyDTO> dtos = policyService.getClientPolicies(clientId)
                .stream()
                .map(InsurancePolicyMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /** Returns all policies, skipping any that fail to map (orphaned / corrupt records). */
    @GetMapping
    public ResponseEntity<List<InsurancePolicyDTO>> getAll() {
        List<InsurancePolicyDTO> result = new ArrayList<>();
        for (InsurancePolicy p : policyService.all()) {
            try {
                InsurancePolicyDTO dto = InsurancePolicyMapper.toDTO(p);
                if (dto != null) result.add(dto);
            } catch (Exception e) {
                log.warn("Skipping policy id={} during mapping: {}", p.getId(), e.getMessage());
            }
        }
        return ResponseEntity.ok(result);
    }

    /** Lightweight count endpoint for dashboard stats. */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        long total = policyService.all().stream().filter(p -> {
            try { InsurancePolicyMapper.toDTO(p); return true; }
            catch (Exception e) { return false; }
        }).count();
        return ResponseEntity.ok(Map.of("count", total));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
