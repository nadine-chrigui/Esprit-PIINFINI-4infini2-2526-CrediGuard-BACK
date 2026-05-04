package tn.esprit.pi_back.dto.checkout;

import tn.esprit.pi_back.dto.Payment.PaymentResponse;
import tn.esprit.pi_back.dto.delivery.DeliveryResponse;
import tn.esprit.pi_back.dto.order.OrderResponse;

public record CheckoutResponse(
        OrderResponse order,
        DeliveryResponse delivery,
        PaymentResponse payment
) {}