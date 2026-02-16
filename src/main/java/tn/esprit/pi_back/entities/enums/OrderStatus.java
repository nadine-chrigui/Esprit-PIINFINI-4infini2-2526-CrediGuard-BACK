package tn.esprit.pi_back.entities.enums;

public enum OrderStatus {
    PENDING,      // créée, pas encore payée
    PAID,         // payée
    SHIPPED,      // expédiée / en livraison
    DELIVERED,    // livrée
    CANCELED      // annulée
}
