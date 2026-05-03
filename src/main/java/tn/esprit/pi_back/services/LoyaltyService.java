package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.CashbackTransaction;
import tn.esprit.pi_back.entities.LoyaltyAccount;
import tn.esprit.pi_back.entities.enums.CashbackTransactionType;
import tn.esprit.pi_back.entities.enums.LoyaltyLevel;

import java.util.List;

public interface LoyaltyService {
    LoyaltyLevel computeLevel(Long userId);
    LoyaltyAccount getLoyaltyAccount(Long userId);
    void attributeCashback(Long userId, double purchaseAmount, String description);
    void redeemCashback(Long userId, double amount, CashbackTransactionType type, String description);
    void evaluateAllLevels();
    List<CashbackTransaction> getTransactions(Long userId);
}
