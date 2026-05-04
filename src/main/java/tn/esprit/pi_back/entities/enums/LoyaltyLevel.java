package tn.esprit.pi_back.entities.enums;

public enum LoyaltyLevel {
    BRONZE(0.01),
    SILVER(0.03),
    GOLD(0.05),
    ELITE(0.08);

    private final double cashbackRate;

    LoyaltyLevel(double cashbackRate) {
        this.cashbackRate = cashbackRate;
    }

    public double getCashbackRate() {
        return cashbackRate;
    }
}