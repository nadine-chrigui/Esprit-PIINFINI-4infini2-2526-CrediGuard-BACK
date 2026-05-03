package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.LoyaltyAccount;
import tn.esprit.pi_back.entities.CashbackTransaction;
import tn.esprit.pi_back.entities.LoyaltyHistory;
import tn.esprit.pi_back.entities.enums.CashbackTransactionType;
import tn.esprit.pi_back.entities.enums.LoyaltyLevel;
import tn.esprit.pi_back.repositories.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyAccountRepository loyaltyRepo;
    private final CashbackTransactionRepository transactionRepo;
    private final LoyaltyHistoryRepository historyRepo;
    private final ScoringService scoringService;
    private final UserRepository userRepo;
    private final CreditRepository creditRepo;

    @Override
    public LoyaltyAccount getLoyaltyAccount(Long userId) {
        return loyaltyRepo.findByUserId(userId).orElseGet(() -> {
            User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            LoyaltyAccount account = LoyaltyAccount.builder()
                    .user(user)
                    .currentLevel(LoyaltyLevel.BRONZE)
                    .cashbackBalance(0.0)
                    .onTimePayments(0)
                    .accountAgeMonths(1)
                    .build();
            return loyaltyRepo.save(account);
        });
    }

    @Override
    public LoyaltyLevel computeLevel(Long userId) {
        int onTimePayments = countOnTimePayments(userId);
        int accountAgeMonths = calculateAccountAgeMonths(userId);
        double creditScore = scoringService.getProfile(userId).getScore();

        if (onTimePayments >= 24 && creditScore >= 85) return LoyaltyLevel.ELITE;
        if (onTimePayments >= 12 && creditScore >= 70) return LoyaltyLevel.GOLD;
        if (onTimePayments >= 6 && creditScore >= 55) return LoyaltyLevel.SILVER;
        return LoyaltyLevel.BRONZE;
    }

    @Override
    @Transactional
    public void attributeCashback(Long userId, double purchaseAmount, String description) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        LoyaltyLevel level = computeLevel(userId);
        
        updateLoyaltyLevel(account, level);

        double rate = level.getCashbackRate();
        double cashbackAmount = purchaseAmount * rate;

        account.setCashbackBalance(account.getCashbackBalance() + cashbackAmount);
        loyaltyRepo.save(account);

        CashbackTransaction transaction = CashbackTransaction.builder()
                .loyaltyAccount(account)
                .amount(cashbackAmount)
                .description(description)
                .date(LocalDateTime.now())
                .type(CashbackTransactionType.CREDIT_EARNED)
                .build();
        transactionRepo.save(transaction);
        
        log.info("Credited {} cashback to user {} (Rate: {}, Level: {})", cashbackAmount, userId, rate, level);
    }

    @Override
    @Transactional
    public void redeemCashback(Long userId, double amount, CashbackTransactionType type, String description) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        if (account.getCashbackBalance() < amount) {
            throw new RuntimeException("Insufficient cashback balance");
        }

        account.setCashbackBalance(account.getCashbackBalance() - amount);
        loyaltyRepo.save(account);

        CashbackTransaction transaction = CashbackTransaction.builder()
                .loyaltyAccount(account)
                .amount(-amount)
                .description(description)
                .date(LocalDateTime.now())
                .type(type)
                .build();
        transactionRepo.save(transaction);
        
        log.info("Redeemed {} cashback for user {} (Type: {})", amount, userId, type);
    }

    @Override
    public List<CashbackTransaction> getTransactions(Long userId) {
        LoyaltyAccount account = getLoyaltyAccount(userId);
        return transactionRepo.findByLoyaltyAccountIdOrderByDateDesc(account.getId());
    }

    @Override
    @Scheduled(cron = "0 0 1 * * *") // Run at 1 AM every day
    @Transactional
    public void evaluateAllLevels() {
        log.info("Starting daily loyalty level evaluation...");
        List<LoyaltyAccount> accounts = loyaltyRepo.findAll();
        for (LoyaltyAccount account : accounts) {
            LoyaltyLevel newLevel = computeLevel(account.getUser().getId());
            updateLoyaltyLevel(account, newLevel);
        }
        log.info("Finished daily loyalty level evaluation.");
    }

    private void updateLoyaltyLevel(LoyaltyAccount account, LoyaltyLevel newLevel) {
        if (account.getCurrentLevel() != newLevel) {
            LoyaltyHistory history = LoyaltyHistory.builder()
                    .loyaltyAccount(account)
                    .previousLevel(account.getCurrentLevel())
                    .newLevel(newLevel)
                    .changeDate(LocalDateTime.now())
                    .build();
            historyRepo.save(history);
            
            account.setCurrentLevel(newLevel);
            loyaltyRepo.save(account);
            log.info("User {} changed loyalty level from {} to {}", account.getUser().getId(), history.getPreviousLevel(), newLevel);
        }
    }

    private int countOnTimePayments(Long userId) {
        // Simplified: Count credits that are fully paid or have a good repayment rate
        List<Credit> credits = creditRepo.findByClientId(userId);
        int onTime = 0;
        for (Credit c : credits) {
            if (c.getMontantRestant() <= 0 || "Complété".equals(c.getStatut())) {
                onTime += 12; // Approximation: 12 on-time payments for a completed credit
            } else if ("En cours".equals(c.getStatut())) {
                onTime += 3; // Approximation
            }
        }
        return onTime;
    }

    private int calculateAccountAgeMonths(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        LocalDateTime createdAt = user.getCreatedAt();
        if (createdAt == null) return 1;
        long months = ChronoUnit.MONTHS.between(createdAt, LocalDateTime.now());
        return (int) Math.max(1, months);
    }
}
