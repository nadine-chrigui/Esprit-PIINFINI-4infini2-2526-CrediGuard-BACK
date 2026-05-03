package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.*;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final FinancialProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final CreditRepository creditRepo;
    private final TransactionRepository transactionRepo;
    private final CompteFinancierRepository compteRepo;

    @Override
    public FinancialProfile calculateAndSaveProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double repaymentRate = calculateRepaymentRate(userId);
        double savingsRate = calculateSavingsRate(userId);
        double historyScore = calculateHistoryScore(userId);

        // Simple weighted score: 40% repayment, 30% savings, 30% history
        double finalScore = (repaymentRate * 0.4) + (savingsRate * 0.3) + (historyScore * 0.3);

        ProfileType type;
        if (finalScore >= 75) type = ProfileType.DYNAMIQUE;
        else if (finalScore >= 50) type = ProfileType.EQUILIBRE;
        else type = ProfileType.PRUDENT;

        FinancialProfile profile = profileRepo.findByUserId(userId)
                .orElse(FinancialProfile.builder().user(user).build());

        profile.setScore(finalScore);
        profile.setProfileType(type);
        profile.setRepaymentRate(repaymentRate);
        profile.setSavingsRate(savingsRate);
        profile.setHistoryScore(historyScore);
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepo.save(profile);
    }

    @Override
    public FinancialProfile getProfile(Long userId) {
        return profileRepo.findByUserId(userId)
                .orElseGet(() -> calculateAndSaveProfile(userId));
    }

    private double calculateRepaymentRate(Long userId) {
        List<Credit> credits = creditRepo.findByClientId(userId);
        if (credits.isEmpty()) return 100.0;

        double totalRate = 0;
        for (Credit c : credits) {
            double paid = c.getMontantTotal() - c.getMontantRestant();
            totalRate += (paid / c.getMontantTotal()) * 100;
        }
        return totalRate / credits.size();
    }

    private double calculateSavingsRate(Long userId) {
        CompteFinancier compte = compteRepo.findByUtilisateur_Id(userId).orElse(null);
        if (compte == null) return 0.0;

        List<Transaction> transactions = transactionRepo.findByCompteSource_IdCompte(compte.getIdCompte());
        if (transactions.isEmpty()) return 0.0;

        double income = 0;
        double expenses = 0;

        for (Transaction t : transactions) {
            if (t.getTypeTransaction() == TransactionType.VENTE) income += t.getMontant();
            else if (t.getTypeTransaction() == TransactionType.ACHAT) expenses += t.getMontant();
        }

        if (income == 0) return 0.0;
        double rate = ((income - expenses) / income) * 100;
        return Math.max(0, Math.min(100, rate));
    }

    private double calculateHistoryScore(Long userId) {
        List<Credit> credits = creditRepo.findByClientId(userId);
        return Math.min(100, credits.size() * 10.0);
    }
}
