package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.CalendarEvent;
import tn.esprit.pi_back.entities.Promotion;
import tn.esprit.pi_back.entities.enums.PromotionStatus;
import tn.esprit.pi_back.repositories.PromotionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionScheduleService {

    private final PromotionRepository promotionRepository;

    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    @Transactional
    public void synchronizePromotionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findAll();
        List<Promotion> changedPromotions = new ArrayList<>();

        for (Promotion promotion : promotions) {
            PromotionStatus resolvedStatus = resolveStatus(promotion, now);

            if (promotion.getStatus() != resolvedStatus) {
                promotion.setStatus(resolvedStatus);
                changedPromotions.add(promotion);
            }
        }

        if (!changedPromotions.isEmpty()) {
            promotionRepository.saveAll(changedPromotions);
        }
    }

    public PromotionStatus resolveStatus(Promotion promotion, LocalDateTime now) {
        if (promotion == null
                || !Boolean.TRUE.equals(promotion.getActive())
                || !Boolean.TRUE.equals(promotion.getAutoApply())) {
            return PromotionStatus.DISABLED;
        }

        LocalDateTime start = promotion.getStartDate();
        LocalDateTime end = promotion.getEndDate();

        CalendarEvent event = promotion.getCalendarEvent();
        if (event != null) {
            if (!Boolean.TRUE.equals(event.getActive())) {
                return PromotionStatus.DISABLED;
            }

            start = event.getStartDate();
            end = event.getEndDate();
        }

        if (start != null && now.isBefore(start)) {
            return PromotionStatus.SCHEDULED;
        }

        if (end != null && now.isAfter(end)) {
            return PromotionStatus.EXPIRED;
        }

        return PromotionStatus.LIVE;
    }
}
