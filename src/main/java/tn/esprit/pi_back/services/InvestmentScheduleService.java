package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.enums.PaymentScheduleFrequency;

public interface InvestmentScheduleService {
    void generateSchedule(Investment investment, Integer durationYears, PaymentScheduleFrequency frequency, Double annualInterestRate);
}
