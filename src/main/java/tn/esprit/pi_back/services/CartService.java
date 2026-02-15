package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.cart.*;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addItem(AddItemRequest req);
    CartResponse updateItem(Long itemId, UpdateItemRequest req);
    CartResponse removeItem(Long itemId);
    CartResponse clear();
}