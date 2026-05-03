package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardEcosystemService {

    private final CarteRepository carteRepository;
    private final CompteFinancierRepository compteRepo;
    private final TransactionRepository transactionRepo;
    private final CashbackRepository cashbackRepo;
    private final CreditRepository creditRepo;
    private final FraudAlertRepository fraudAlertRepo;
    private final FinancialScoreRepository scoreRepo;
    private final AiInsightRepository insightRepo;

    /**
     * Workflow A: Merchant Payment
     */
    @Transactional
    public Transaction processPayment(String cardNumber, Double amount, String merchant, String category) {
        Carte carte = carteRepository.findByCardNumberAndActiveTrue(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Card not found or inactive"));

        CompteFinancier compte = carte.getCompte();
        Double totalCapacity = (compte.getSolde() != null ? compte.getSolde() : 0.0) + 
                             (compte.getCreditDisponible() != null ? compte.getCreditDisponible() : 0.0);

        if (totalCapacity < amount) {
            saveTransaction(compte, carte, amount, merchant, category, TransactionStatut.FAILED);
            throw new IllegalStateException("Insufficient balance and credit");
        }

        Double remainingToDebit = amount;
        Double solde = compte.getSolde() != null ? compte.getSolde() : 0.0;
        
        if (solde >= remainingToDebit) {
            compte.setSolde(solde - remainingToDebit);
        } else {
            Double fromBalance = solde;
            Double fromCredit = remainingToDebit - fromBalance;
            compte.setSolde(0.0);
            compte.setCreditUtilise((compte.getCreditUtilise() != null ? compte.getCreditUtilise() : 0.0) + fromCredit);
            compte.setCreditDisponible((compte.getCreditDisponible() != null ? compte.getCreditDisponible() : 0.0) - fromCredit);
        }

        Transaction transaction = saveTransaction(compte, carte, amount, merchant, category, TransactionStatut.COMPLETED);
        applyCashback(compte, transaction, amount, carte.getLoyaltyLevel());
        compteRepo.save(compte);
        
        try {
            analyzeFraud(transaction, carte);
            updateFinancialScore(compte.getUtilisateur());
            generateInsights(compte.getUtilisateur());
        } catch (Exception e) {
            log.error("Secondary payment tasks failed but transaction was saved", e);
        }

        return transaction;
    }

    /**
     * Workflow B: Auto-reimbursement
     */
    @Transactional
    public void processDeposit(CompteFinancier compte, Double amount) {
        List<Credit> activeCredits = creditRepo.findByCompteIdCompteAndStatut(compte.getIdCompte(), StatutCredit.ACTIF);
        if (activeCredits.isEmpty()) return;

        Double amountToReimburseTotal = amount * 0.20; 
        Double remainingToReimburse = amountToReimburseTotal;

        for (Credit credit : activeCredits) {
            if (remainingToReimburse <= 0) break;

            Double creditRestant = credit.getMontantRestant();
            Double payment = Math.min(remainingToReimburse, creditRestant);

            credit.setMontantRestant(creditRestant - payment);
            if (credit.getMontantRestant() <= 0) {
                credit.setStatut(StatutCredit.CLOTURE);
                Double limite = compte.getCreditLimite() != null ? compte.getCreditLimite() : 0.0;
                Double accorde = credit.getMontantAccorde() != null ? credit.getMontantAccorde() : 0.0;
                compte.setCreditUtilise(Math.max(0.0, (compte.getCreditUtilise() != null ? compte.getCreditUtilise() : 0.0) - accorde));
                compte.setCreditDisponible(limite - compte.getCreditUtilise());
            }
            creditRepo.save(credit);

            Transaction tx = new Transaction();
            tx.setCompteSource(compte);
            tx.setMontant(payment);
            tx.setTypeTransaction(TransactionType.REMBOURSEMENT);
            tx.setStatut(TransactionStatut.COMPLETED);
            tx.setMarchand("Auto Reimbursement");
            tx.setCategorie("FINANCE");
            tx.setOrderId(0L);
            transactionRepo.save(tx);

            remainingToReimburse -= payment;
            compte.setSolde((compte.getSolde() != null ? compte.getSolde() : 0.0) - payment);
        }
        compteRepo.save(compte);
    }

    private Transaction saveTransaction(CompteFinancier compte, Carte carte, Double amount, String merchant, String category, TransactionStatut status) {
        Transaction tx = new Transaction();
        tx.setCompteSource(compte);
        tx.setCarte(carte);
        tx.setMontant(amount);
        tx.setTypeTransaction(TransactionType.PAIEMENT);
        tx.setStatut(status);
        tx.setMarchand(merchant);
        tx.setCategorie(category);
        tx.setDateTransaction(LocalDateTime.now());
        tx.setTypeTransaction(TransactionType.ACHAT);
        tx.setPaymentSource(PaymentSource.CARD);
        tx.setOrderId(0L);
        return transactionRepo.save(tx);
    }

    private void applyCashback(CompteFinancier compte, Transaction transaction, Double amount, tn.esprit.pi_back.entities.LoyaltyLevel level) {
        Double rate = (level != null && level.getCashbackRate() != null) ? level.getCashbackRate() : 1.0;
        Double cashbackAmount = (amount * rate) / 100.0;

        Cashback cb = new Cashback();
        cb.setAmount(cashbackAmount);
        cb.setAppliedRate(rate);
        cb.setTransaction(transaction);
        cb.setCompte(compte);
        cashbackRepo.save(cb);

        compte.setSolde((compte.getSolde() != null ? compte.getSolde() : 0.0) + cashbackAmount);
    }

    private void analyzeFraud(Transaction tx, Carte carte) {
        int riskScore = 0;
        if (tx.getMontant() > 1000) riskScore += 30;
        if (riskScore >= 30) {
            FraudAlert alert = new FraudAlert();
            alert.setAlertType("MONTANT_CRITIQUE");
            alert.setRiskScore(riskScore);
            alert.setStatus("SURVEILLANCE");
            alert.setTransaction(tx);
            alert.setCarte(carte);
            fraudAlertRepo.save(alert);
        }
    }

    private void updateFinancialScore(User user) {
        if (user == null) return;
        FinancialScore score = scoreRepo.findByUserId(user.getId())
                .orElse(new FinancialScore());
        score.setUser(user);
        score.setScore(750);
        score.setLevel("BON");
        scoreRepo.save(score);
    }

    private void generateInsights(User user) {
        if (user == null) return;
        AiInsight insight = new AiInsight();
        insight.setUser(user);
        insight.setType("CONSEIL_BUDGET");
        insight.setMessage("Vous dépensez beaucoup en restauration ce mois-ci. Envisagez de cuisiner davantage pour économiser.");
        insight.setPotentialSavings(50.0);
        insightRepo.save(insight);
    }

    public FinancialScore getScoreForUser(Long userId) {
        return scoreRepo.findByUserId(userId).orElse(null);
    }

    public List<AiInsight> getInsightsForUser(Long userId) {
        return insightRepo.findByUserIdAndViewedFalse(userId);
    }

    public Carte getCardByCompte(Long compteId) {
        return carteRepository.findAll().stream()
                .filter(c -> c.getCompte() != null && c.getCompte().getIdCompte().equals(compteId))
                .findFirst().orElse(null);
    }

    public List<Cashback> getCashbacksByCompte(Long compteId) {
        return cashbackRepo.findAll().stream()
                .filter(c -> c.getCompte() != null && c.getCompte().getIdCompte().equals(compteId))
                .toList();
    }
}
