package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.events.OrderPaidEvent;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRemboursementService {

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("OrderRemboursementService est BIEN CHARGE par Spring !");
    }

    private final RegleRemboursementRepository regleRepo;
    private final TransactionRepository transactionRepo;
    private final RemboursementRepository remboursementRepo;
    private final CompteFinancierRepository compteRepo;
    private final OrderRepository orderRepo;
    private final CreditRepository creditRepo;
    private final UserRepository userRepo;

    @Transactional
    public void onOrderPaid(tn.esprit.pi_back.events.OrderPaidEvent event) {
        Order order = event.getOrder();
        User user = order.getUser();
        log.info("Traitement de la commande PAYEE: {}", order.getId());

        // 1. Récupère le crédit actif du bénéficiaire
        List<Credit> credits = creditRepo.findByClientId(user.getId());
        Credit activeCredit = credits.stream()
                .filter(c -> c.getStatut() != null && ("ACTIF".equals(c.getStatut().name()) || "PENDING".equals(c.getStatut().name()) || "APPROVED".equals(c.getStatut().name())))
                .findFirst()
                .orElse(null);

        if (activeCredit == null) {
            log.warn("Aucun crédit actif trouvé pour l'utilisateur {}", user.getId());
            return;
        }

        // 2. Récupère la règle de remboursement du bénéficiaire
        List<RegleRemboursement> regles = regleRepo.findByCreditId(activeCredit.getId());
        if (regles.isEmpty()) {
            log.warn("Aucune règle de remboursement trouvée pour le crédit {}", activeCredit.getId());
            return;
        }
        RegleRemboursement regle = regles.stream()
                .filter(r -> r.getTypeRegle() == tn.esprit.pi_back.entities.enums.RegleType.POURCENTAGE_SUR_VENTE)
                .findFirst()
                .orElse(regles.get(0));

        // 3. Calcule le montant depuis order.totalAmount
        double montantRembourse = order.getTotalAmount() * regle.getValeur() / 100.0;

        // 4. Crée la Transaction pivot
        CompteFinancier compteClient = compteRepo.findByUtilisateur_Id(user.getId()).orElse(null);
        if (compteClient == null) {
            log.warn("Aucun compte financier trouvé pour le client {}", user.getId());
            return;
        }
        
        // On cherche le compte de la plateforme (admin)
        // Par défaut on cherche un admin et on prend son compte
        User adminUser = userRepo.findAll().stream()
                .filter(u -> u.getUserType() == UserType.ADMIN)
                .findFirst()
                .orElse(null);
                
        CompteFinancier comptePlateforme = adminUser != null ? compteRepo.findByUtilisateur_Id(adminUser.getId()).orElse(null) : null;

        Transaction tx = new Transaction();
        tx.setTypeTransaction(TransactionType.REMBOURSEMENT);
        tx.setMontant(montantRembourse);
        tx.setCompteSource(compteClient);
        tx.setCompteDestination(comptePlateforme);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setStatut(TransactionStatut.COMPLETED);
        tx.setOrderId(order.getId());
        tx = transactionRepo.save(tx);

        // 5. Enregistre le Remboursement lié à la transaction
        Remboursement remb = new Remboursement();
        remb.setTransaction(tx);
        remb.setCredit(activeCredit);
        remb.setMontant(montantRembourse);
        remb.setOrderReference(order.getReference());
        remb.setMode("automatique");
        remb.setDateRemboursement(LocalDateTime.now());
        remboursementRepo.save(remb);

        // 6. Déduit du crédit restant
        double reste = activeCredit.getMontantRestant() != null ? activeCredit.getMontantRestant() : activeCredit.getMontantTotal();
        activeCredit.setMontantRestant(reste - montantRembourse);
        creditRepo.save(activeCredit);

        // 7. Met à jour Order.paidAt
        order.setPaidAt(LocalDateTime.now());
        orderRepo.save(order);
        
        log.info("Remboursement automatisé de {} TND effectué avec succès pour la commande {}", montantRembourse, order.getId());
    }
}
