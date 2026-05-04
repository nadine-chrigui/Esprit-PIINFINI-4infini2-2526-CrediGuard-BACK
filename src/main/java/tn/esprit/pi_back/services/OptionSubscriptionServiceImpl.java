package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.OptionSubscription.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionSubscriptionServiceImpl implements OptionSubscriptionService {

    private final OptionSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PurchaseOptionRepository purchaseOptionRepository;

    @Override
    public OptionSubscriptionResponse create(OptionSubscriptionCreateRequest req) {
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PurchaseOption option = purchaseOptionRepository.findById(req.purchaseOptionId())
                .orElseThrow(() -> new RuntimeException("PurchaseOption not found"));

        if (option.getStatus() != PurchaseOption.OptionStatus.ACTIVE) {
            throw new IllegalStateException("PurchaseOption is not active");
        }
        if (req.reservedQuantity() > option.getRemainingQuantity()) {
            throw new IllegalArgumentException("reservedQuantity exceeds remaining stock");
        }

        OptionSubscription sub = new OptionSubscription();
        sub.setReservedQuantity(req.reservedQuantity());
        sub.setSubscriptionDate(req.subscriptionDate());
        sub.setAmountPaid(req.amountPaid());
        sub.setUser(user);
        sub.setPurchaseOption(option);

        option.setSoldQuantity(option.getSoldQuantity() + req.reservedQuantity());
        option.setRemainingQuantity(option.getRemainingQuantity() - req.reservedQuantity());
        if (option.getRemainingQuantity() == 0) {
            option.setStatus(PurchaseOption.OptionStatus.SOLD_OUT);
        }

        return map(subscriptionRepository.save(sub));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionSubscriptionResponse> getAll() {
        return subscriptionRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OptionSubscriptionResponse getById(Long id) {
        return map(subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OptionSubscription not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionSubscriptionResponse> getByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionSubscriptionResponse> getByOption(Long optionId) {
        return subscriptionRepository.findByPurchaseOptionOptionId(optionId).stream().map(this::map).toList();
    }

    @Override
    public OptionSubscriptionResponse update(Long id, OptionSubscriptionUpdateRequest req) {
        OptionSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OptionSubscription not found"));
        if (req.status() != null && req.status() != sub.getStatus()) {
            PurchaseOption option = sub.getPurchaseOption();
            if (req.status() == OptionSubscription.SubscriptionStatus.CANCELLED) {
                option.setSoldQuantity(Math.max(0, option.getSoldQuantity() - sub.getReservedQuantity()));
                option.setRemainingQuantity(option.getRemainingQuantity() + sub.getReservedQuantity());
                if (option.getStatus() == PurchaseOption.OptionStatus.SOLD_OUT && option.getRemainingQuantity() > 0) {
                    option.setStatus(PurchaseOption.OptionStatus.ACTIVE);
                }
            }
            sub.setStatus(req.status());
        }
        return map(sub);
    }

    @Override
    public void delete(Long id) {
        OptionSubscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OptionSubscription not found"));
        if (sub.getStatus() != OptionSubscription.SubscriptionStatus.CANCELLED) {
            PurchaseOption option = sub.getPurchaseOption();
            option.setSoldQuantity(Math.max(0, option.getSoldQuantity() - sub.getReservedQuantity()));
            option.setRemainingQuantity(option.getRemainingQuantity() + sub.getReservedQuantity());
            if (option.getStatus() == PurchaseOption.OptionStatus.SOLD_OUT && option.getRemainingQuantity() > 0) {
                option.setStatus(PurchaseOption.OptionStatus.ACTIVE);
            }
        }
        subscriptionRepository.delete(sub);
    }

    private OptionSubscriptionResponse map(OptionSubscription s) {
        return new OptionSubscriptionResponse(
                s.getSubscriptionId(),
                s.getReservedQuantity(),
                s.getSubscriptionDate(),
                s.getAmountPaid(),
                s.getStatus().name(),
                s.getUser().getId(),
                s.getPurchaseOption().getOptionId()
        );
    }
}
