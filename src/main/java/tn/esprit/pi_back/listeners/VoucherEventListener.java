package tn.esprit.pi_back.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.Voucher;
import tn.esprit.pi_back.entities.enums.VoucherStatus;
import tn.esprit.pi_back.events.CreditApprovedEvent;
import tn.esprit.pi_back.services.IVoucherService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherEventListener {

    private final IVoucherService voucherService;

    /**
     * Utilisation de @EventListener synchrone pour rester dans la même transaction
     * et éviter les problèmes de LazyInitializationException.
     */
    @EventListener
    @Transactional
    public void onCreditApproved(CreditApprovedEvent event) {
        System.out.println(">>>> EVENT RECEIVED: Credit Ref " + event.getDemandeCredit().getReference());
        log.info("🔔 Déclenchement de la génération de voucher pour le crédit: {}", event.getDemandeCredit().getReference());
        
        try {
            Voucher voucher = new Voucher();
            
            // On s'assure d'avoir le client
            if (event.getDemandeCredit().getClient() == null) {
                log.error("❌ Impossible de générer le voucher : Le client est null pour le crédit {}", event.getDemandeCredit().getReference());
                return;
            }

            voucher.setClient(event.getDemandeCredit().getClient());
            voucher.setAmount(BigDecimal.valueOf(event.getDemandeCredit().getMontantDemande()));
            voucher.setDemandeCredit(event.getDemandeCredit()); 
            
            // Code unique et validité
            voucher.setCode("VOUCHER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            voucher.setStatus(VoucherStatus.ACTIVE);
            voucher.setExpirationDate(LocalDate.now().plusMonths(12)); 
            
            voucherService.createVoucher(voucher);
            
            log.info("✅ Voucher {} généré avec succès pour le client {}", voucher.getCode(), event.getDemandeCredit().getClient().getFullName());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération automatique du voucher : {}", e.getMessage(), e);
        }
    }
}
