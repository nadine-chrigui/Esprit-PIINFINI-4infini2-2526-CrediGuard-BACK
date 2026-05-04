package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.OptionRedemption.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionRedemptionServiceImpl implements OptionRedemptionService {

    private final OptionRedemptionRepository redemptionRepository;
    private final OptionSubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;

    @Override
    public OptionRedemptionResponse create(OptionRedemptionCreateRequest req) {
        OptionSubscription subscription = subscriptionRepository.findById(req.subscriptionId())
                .orElseThrow(() -> new RuntimeException("OptionSubscription not found"));

        if (redemptionRepository.findBySubscriptionSubscriptionId(req.subscriptionId()).isPresent()) {
            throw new IllegalStateException("This subscription has already been redeemed");
        }

        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OptionRedemption redemption = new OptionRedemption();
        redemption.setRedeemedQuantity(req.redeemedQuantity());
        redemption.setRedemptionDate(req.redemptionDate());
        redemption.setFinalPrice(req.finalPrice());
        redemption.setCommissionAmount(req.commissionAmount());
        redemption.setSubscription(subscription);
        redemption.setOrder(order);

        // Mark subscription as PAID
        subscription.setStatus(OptionSubscription.SubscriptionStatus.PAID);

        return map(redemptionRepository.save(redemption));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionRedemptionResponse> getAll() {
        return redemptionRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OptionRedemptionResponse getById(Long id) {
        return map(redemptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OptionRedemption not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public OptionRedemptionResponse getBySubscription(Long subscriptionId) {
        return map(redemptionRepository.findBySubscriptionSubscriptionId(subscriptionId)
                .orElseThrow(() -> new RuntimeException("No redemption found for this subscription")));
    }

    @Override
    public void delete(Long id) {
        redemptionRepository.deleteById(id);
    }

    private OptionRedemptionResponse map(OptionRedemption r) {
        return new OptionRedemptionResponse(
                r.getRedemptionId(),
                r.getRedeemedQuantity(),
                r.getRedemptionDate(),
                r.getFinalPrice(),
                r.getCommissionAmount(),
                r.getSubscription().getSubscriptionId(),
                r.getOrder().getId()
        );
    }
}
