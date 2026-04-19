package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.cart.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.entities.enums.CartStatus;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User me = userService.getCurrentUserOrThrow(); // ✅ PAS getOrCreateCurrentUser
        Cart cart = getOrCreateActiveCart(me);
        return toResponse(cart);
    }
    @Override
    public CartResponse addItem(AddItemRequest req) {
        User me = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateActiveCart(me);

        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.productId()));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is not active.");
        }

        // règle stock (si STANDARD)
        if (product.getStockQuantity() != null && product.getStockQuantity() < req.quantity()) {
            throw new IllegalStateException("Insufficient stock.");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(req.quantity());
            item.setUnitPrice(product.getCurrentPrice() != null ? product.getCurrentPrice() : product.getBasePrice());
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + req.quantity());
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    public CartResponse updateItem(Long itemId, UpdateItemRequest req) {
        User me = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateActiveCart(me);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Forbidden: item not in your cart.");
        }

        item.setQuantity(req.quantity());
        return toResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long itemId) {
        User me = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateActiveCart(me);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("CartItem not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Forbidden: item not in your cart.");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return toResponse(cart);
    }

    @Override
    public CartResponse clear() {
        User me = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateActiveCart(me);

        cart.getItems().clear(); // orphanRemoval=true => delete
        return toResponse(cart);
    }

    private Cart getOrCreateActiveCart(User user) {
        return cartRepository.findByUserIdAndStatus(user.getId(), CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    c.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(c);
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            double price = i.getUnitPrice();
            int qty = i.getQuantity();
            return new CartItemResponse(
                    i.getId(),
                    i.getProduct().getId(),
                    i.getProduct().getName(),
                    price,
                    qty,
                    price * qty
            );
        }).toList();

        double total = items.stream().mapToDouble(CartItemResponse::lineTotal).sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getStatus(),
                items,
                total
        );
    }
}