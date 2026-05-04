package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.ReturnPayment;
import tn.esprit.pi_back.entities.enums.PaymentScheduleFrequency;
import tn.esprit.pi_back.repositories.ReturnPaymentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestmentScheduleServiceImpl implements InvestmentScheduleService {

    private final ReturnPaymentRepository returnPaymentRepository;

    @Value("${crowdfunding.return-payment.monthly-day:22}")
    private int monthlyPaymentDay;

    @Override
    public void generateSchedule(Investment investment, Integer durationYears, PaymentScheduleFrequency frequency, Double annualInterestRate) {
        if (!returnPaymentRepository.findByInvestmentInvestmentId(investment.getInvestmentId()).isEmpty()) {
            return;
        }

        int periods = frequency == PaymentScheduleFrequency.MONTHLY ? durationYears * 12 : durationYears;
        double totalPayable = investment.getAmount() * Math.pow(1 + (annualInterestRate / 100.0), durationYears);
        double totalInterest = Math.max(0.0, totalPayable - investment.getAmount());
        double interestPerPeriod = periods == 0 ? 0.0 : totalInterest / periods;

        List<ReturnPayment> schedule = new ArrayList<>();
        LocalDate baseDate = investment.getInvestmentDate();

        if (round(interestPerPeriod) > 0.0) {
            for (int index = 1; index <= periods; index++) {
                ReturnPayment interestPayment = new ReturnPayment();
                interestPayment.setAmount(round(interestPerPeriod));
                interestPayment.setPaymentDate(frequency == PaymentScheduleFrequency.MONTHLY
                        ? resolveMonthlyPaymentDate(baseDate, index)
                        : baseDate.plusYears(index));
                interestPayment.setType(ReturnPayment.ReturnType.INTEREST);
                interestPayment.setStatus(ReturnPayment.ReturnStatus.SCHEDULED);
                interestPayment.setInvestment(investment);
                schedule.add(interestPayment);
            }
        }

        ReturnPayment capitalPayment = new ReturnPayment();
        capitalPayment.setAmount(round(investment.getAmount()));
        capitalPayment.setPaymentDate(frequency == PaymentScheduleFrequency.MONTHLY
                ? resolveMonthlyPaymentDate(baseDate, periods)
                : baseDate.plusYears(durationYears));
        capitalPayment.setType(ReturnPayment.ReturnType.CAPITAL);
        capitalPayment.setStatus(ReturnPayment.ReturnStatus.SCHEDULED);
        capitalPayment.setInvestment(investment);
        schedule.add(capitalPayment);

        returnPaymentRepository.saveAll(schedule);
    }

    private double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDate resolveMonthlyPaymentDate(LocalDate baseDate, int monthOffset) {
        LocalDate targetMonth = baseDate.plusMonths(monthOffset);
        int safeDay = Math.max(1, Math.min(monthlyPaymentDay, targetMonth.lengthOfMonth()));
        return targetMonth.withDayOfMonth(safeDay);
    }
}
