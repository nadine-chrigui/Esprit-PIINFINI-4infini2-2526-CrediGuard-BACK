package tn.esprit.pi_back.dto.checkout;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pi_back.entities.enums.DeliverySlot;
import tn.esprit.pi_back.entities.enums.DeliveryType;
import tn.esprit.pi_back.entities.enums.PaymentType;

import java.time.LocalDateTime;

public record CheckoutRequest(

        @NotNull(message = "addressId is required")
        Long addressId,

        @NotNull(message = "paymentType is required")
        PaymentType paymentType,

        @NotNull(message = "deliveryType is required")
        DeliveryType deliveryType,

        DeliverySlot deliverySlot,

        LocalDateTime scheduledAt,

        String promoCode
) {}