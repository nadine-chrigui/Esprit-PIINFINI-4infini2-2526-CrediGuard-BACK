package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.cart.AddItemRequest;
import tn.esprit.pi_back.dto.cart.CartItemResponse;
import tn.esprit.pi_back.dto.cart.CartResponse;
import tn.esprit.pi_back.dto.cart.UpdateItemRequest;
import tn.esprit.pi_back.dto.promotion.ProductPriceView;
import tn.esprit.pi_back.entities.Cart;
import tn.esprit.pi_back.entities.CartItem;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.ProductRequestOffer;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.CartItemSource;
import tn.esprit.pi_back.entities.enums.CartStatus;
import tn.esprit.pi_back.entities.enums.ProductRequestOfferStatus;
import tn.esprit.pi_back.repositories.CartItemRepository;
import tn.esprit.pi_back.repositories.CartRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.ProductRequestOfferRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductRequestOfferRepository productRequestOfferRepository;
    private final UserService userService;
    private final PromotionService promotionService;

    @Override
    @Transactional
    public CartResponse getMyCart() {
        User me = userService.getOrCreateCurrentUser();
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

        if (product.getStockQuantity() != null && product.getStockQuantity() < req.quantity()) {
            throw new IllegalStateException("Insufficient stock.");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        double storedUnitPrice = product.getCurrentPrice() != null
                ? product.getCurrentPrice()
                : product.getBasePrice();

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(req.quantity());
            item.setUnitPrice(storedUnitPrice);
            item.setSource(CartItemSource.STANDARD);
            item.setSourceOfferId(null);
            item.setNegotiatedUnitPrice(null);
            cart.getItems().add(item);
        } else {
            int newQty = item.getQuantity() + req.quantity();

            if (product.getStockQuantity() != null && product.getStockQuantity() < newQty) {
                throw new IllegalStateException("Insufficient stock.");
            }

            item.setQuantity(newQty);
            item.setUnitPrice(storedUnitPrice);
            item.setSource(CartItemSource.STANDARD);
            item.setSourceOfferId(null);
            item.setNegotiatedUnitPrice(null);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    public CartResponse addAcceptedOfferToCart(Long offerId) {
        User me = userService.getOrCreateCurrentUser();
        Cart cart = getOrCreateActiveCart(me);

        ProductRequestOffer offer = productRequestOfferRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));

        if (offer.getStatus() != ProductRequestOfferStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted offers can be added to cart.");
        }

        Product product = offer.getProduct();

        if (product == null) {
            throw new IllegalStateException("Accepted offer must be linked to a product.");
        }

        Integer quantity = offer.getProposedQuantity();
        Double negotiatedPrice = offer.getProposedPrice();

        if (quantity == null || quantity <= 0) {
            throw new IllegalStateException("Accepted offer has invalid quantity.");
        }

        if (negotiatedPrice == null || negotiatedPrice <= 0) {
            throw new IllegalStateException("Accepted offer has invalid price.");
        }

        if (!product.isActive()) {
            throw new IllegalStateException("Product is not active.");
        }

        if (product.getStockQuantity() != null && product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock.");
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(negotiatedPrice);
            item.setNegotiatedUnitPrice(negotiatedPrice);
            item.setSourceOfferId(offerId);
            item.setSource(CartItemSource.PRODUCT_REQUEST_OFFER);
            cart.getItems().add(item);
        } else {
            item.setQuantity(quantity);
            item.setUnitPrice(negotiatedPrice);
            item.setNegotiatedUnitPrice(negotiatedPrice);
            item.setSourceOfferId(offerId);
            item.setSource(CartItemSource.PRODUCT_REQUEST_OFFER);
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

        Product product = item.getProduct();
        if (product.getStockQuantity() != null && product.getStockQuantity() < req.quantity()) {
            throw new IllegalStateException("Insufficient stock.");
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

        cart.getItems().clear();
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
        double cartSubtotalForEligibility = cart.getItems().stream()
                .mapToDouble(item -> {
                    Product product = item.getProduct();
                    double basePrice = product.getCurrentPrice() != null
                            ? product.getCurrentPrice()
                            : product.getBasePrice();
                    return basePrice * item.getQuantity();
                })
                .sum();

        List<CartItemResponse> items = cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            ProductPriceView priceView = promotionService.calculateProductPrice(product, cartSubtotalForEligibility);

            boolean fromOffer = item.getSource() == CartItemSource.PRODUCT_REQUEST_OFFER
                    && item.getNegotiatedUnitPrice() != null
                    && item.getNegotiatedUnitPrice() > 0;

            double originalUnitPrice = fromOffer
                    ? item.getNegotiatedUnitPrice()
                    : priceView.originalPrice();

            double finalUnitPrice = fromOffer
                    ? item.getNegotiatedUnitPrice()
                    : priceView.finalPrice();

            double discountAmount = fromOffer
                    ? 0.0
                    : priceView.discountAmount();

            boolean promotionApplied = !fromOffer && Boolean.TRUE.equals(priceView.promotionApplied());

            String promotionName = fromOffer
                    ? null
                    : priceView.promotionName();

            int quantity = item.getQuantity();
            double lineTotal = finalUnitPrice * quantity;

            return new CartItemResponse(
                    item.getId(),
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    item.getUnitPrice(),
                    originalUnitPrice,
                    finalUnitPrice,
                    discountAmount,
                    promotionApplied,
                    promotionName,
                    quantity,
                    lineTotal,
                    item.getSource() != null ? item.getSource().name() : CartItemSource.STANDARD.name(),
                    item.getSourceOfferId(),
                    item.getNegotiatedUnitPrice(),
                    product.isExpressDeliveryAvailable(),
                    product.getExpressDeliveryFee()
            );
        }).toList();

        double subtotal = items.stream()
                .mapToDouble(i -> i.originalUnitPrice() * i.quantity())
                .sum();

        double totalDiscount = items.stream()
                .mapToDouble(i -> (i.originalUnitPrice() - i.finalUnitPrice()) * i.quantity())
                .sum();

        double total = items.stream()
                .mapToDouble(CartItemResponse::lineTotal)
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getStatus(),
                items,
                subtotal,
                totalDiscount,
                total
        );
    }
}
