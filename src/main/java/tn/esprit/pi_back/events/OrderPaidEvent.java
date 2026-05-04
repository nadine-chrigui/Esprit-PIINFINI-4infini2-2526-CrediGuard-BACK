package tn.esprit.pi_back.events;

import org.springframework.context.ApplicationEvent;
import tn.esprit.pi_back.entities.Order;

public class OrderPaidEvent extends ApplicationEvent {
    private final Order order;

    public OrderPaidEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}