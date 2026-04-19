package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.cart.AddItemRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferResponse;
import tn.esprit.pi_back.dto.productrequest.ProductRequestResponse;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.entities.Product;
import tn.esprit.pi_back.entities.ProductRequest;
import tn.esprit.pi_back.entities.ProductRequestOffer;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.ProductRequestOfferStatus;
import tn.esprit.pi_back.entities.enums.ProductRequestStatus;
import tn.esprit.pi_back.repositories.CategoryRepository;
import tn.esprit.pi_back.repositories.ProductRepository;
import tn.esprit.pi_back.repositories.ProductRequestOfferRepository;
import tn.esprit.pi_back.repositories.ProductRequestRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductRequestServiceImpl implements ProductRequestService {

    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestOfferRepository productRequestOfferRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    public ProductRequestResponse createRequest(Long clientId, ProductRequestCreateRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.categoryId()));
        }

        User targetSeller = null;
        if (request.targetSellerId() != null) {
            targetSeller = userRepository.findById(request.targetSellerId())
                    .orElseThrow(() -> new RuntimeException("Seller not found with id: " + request.targetSellerId()));
        }

        ProductRequest entity = ProductRequest.builder()
                .title(request.title())
                .description(request.description())
                .requestedQuantity(request.requestedQuantity())
                .maxBudget(request.maxBudget())
                .desiredDate(request.desiredDate())
                .imageUrl(request.imageUrl())
                .status(ProductRequestStatus.OPEN)
                .client(client)
                .category(category)
                .targetSeller(targetSeller)
                .build();

        ProductRequest saved = productRequestRepository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestResponse> getMyRequests(Long clientId) {
        return productRequestRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestResponse> getOpenRequestsForSellers() {
        return productRequestRepository.findByStatusAndTargetSellerIsNullOrderByCreatedAtDesc(ProductRequestStatus.OPEN)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestResponse> getRequestsForTargetSeller(Long sellerId) {
        return productRequestRepository.findByTargetSellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductRequestOfferResponse createOffer(Long sellerId, Long productRequestId, ProductRequestOfferCreateRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found with id: " + sellerId));

        ProductRequest productRequest = productRequestRepository.findById(productRequestId)
                .orElseThrow(() -> new RuntimeException("Product request not found with id: " + productRequestId));

        if (productRequest.getStatus() != ProductRequestStatus.OPEN
                && productRequest.getStatus() != ProductRequestStatus.OFFERED) {
            throw new RuntimeException("This request is no longer available for offers.");
        }

        if (productRequest.getClient() != null && productRequest.getClient().getId().equals(sellerId)) {
            throw new RuntimeException("You cannot submit an offer to your own request.");
        }

        if (productRequest.getTargetSeller() != null
                && !productRequest.getTargetSeller().getId().equals(sellerId)) {
            throw new RuntimeException("This request is reserved for another seller.");
        }

        Product product = productRepository.findByIdAndSellerIdAndActiveTrue(request.productId(), sellerId)
                .orElseThrow(() -> new RuntimeException("Product not found or does not belong to this seller."));

        ProductRequestOffer offer = ProductRequestOffer.builder()
                .productRequest(productRequest)
                .seller(seller)
                .product(product)
                .proposedPrice(request.proposedPrice())
                .proposedQuantity(request.proposedQuantity())
                .estimatedDeliveryDays(request.estimatedDeliveryDays())
                .message(request.message())
                .status(ProductRequestOfferStatus.PENDING)
                .build();

        ProductRequestOffer savedOffer = productRequestOfferRepository.save(offer);

        if (productRequest.getStatus() == ProductRequestStatus.OPEN) {
            productRequest.setStatus(ProductRequestStatus.OFFERED);
            productRequestRepository.save(productRequest);
        }

        return mapOfferToResponse(savedOffer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestOfferResponse> getOffersForRequest(Long productRequestId) {
        return productRequestOfferRepository.findByProductRequestIdOrderByCreatedAtDesc(productRequestId)
                .stream()
                .map(this::mapOfferToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestOfferResponse> getMyOffers(Long sellerId) {
        return productRequestOfferRepository.findBySellerIdOrderByCreatedAtDesc(sellerId)
                .stream()
                .map(this::mapOfferToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRequestResponse getRequestById(Long requestId) {
        ProductRequest entity = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Product request not found with id: " + requestId));
        return mapToResponse(entity);
    }

    @Override
    public ProductRequestOfferResponse acceptOffer(Long clientId, Long offerId) {
        ProductRequestOffer selectedOffer = productRequestOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        ProductRequest productRequest = selectedOffer.getProductRequest();
        if (productRequest == null) {
            throw new RuntimeException("This offer is not linked to a product request.");
        }

        ProductRequest ownedRequest = productRequestRepository
                .findByIdAndClientId(productRequest.getId(), clientId)
                .orElseThrow(() -> new RuntimeException("Request not found for this client."));

        if (ownedRequest.getStatus() == ProductRequestStatus.CANCELLED
                || ownedRequest.getStatus() == ProductRequestStatus.CLOSED
                || ownedRequest.getStatus() == ProductRequestStatus.REJECTED) {
            throw new RuntimeException("This request is closed and cannot be updated.");
        }

        if (selectedOffer.getStatus() == ProductRequestOfferStatus.REJECTED) {
            throw new RuntimeException("Rejected offer cannot be accepted.");
        }

        if (selectedOffer.getProduct() == null) {
            throw new RuntimeException("Accepted offer must be linked to a product.");
        }

        if (selectedOffer.getProposedPrice() == null || selectedOffer.getProposedPrice() <= 0) {
            throw new RuntimeException("Accepted offer has invalid proposed price.");
        }

        if (selectedOffer.getProposedQuantity() == null || selectedOffer.getProposedQuantity() <= 0) {
            throw new RuntimeException("Accepted offer has invalid proposed quantity.");
        }

        List<ProductRequestOffer> offers = productRequestOfferRepository.findByProductRequestId(ownedRequest.getId());

        for (ProductRequestOffer offer : offers) {
            if (offer.getId().equals(offerId)) {
                offer.setStatus(ProductRequestOfferStatus.ACCEPTED);
            } else {
                offer.setStatus(ProductRequestOfferStatus.REJECTED);
            }
        }

        productRequestOfferRepository.saveAll(offers);

        ownedRequest.setStatus(ProductRequestStatus.ACCEPTED);
        productRequestRepository.save(ownedRequest);

        // Add accepted offer product to active cart instead of creating an order directly
        cartService.addItem(
                new AddItemRequest(
                        selectedOffer.getProduct().getId(),
                        selectedOffer.getProposedQuantity()
                )
        );

        ProductRequestOffer refreshed = productRequestOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Accepted offer not found after update."));

        return mapOfferToResponse(refreshed);
    }

    @Override
    public ProductRequestOfferResponse rejectOffer(Long clientId, Long offerId) {
        ProductRequestOffer offer = productRequestOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + offerId));

        ProductRequest productRequest = offer.getProductRequest();
        if (productRequest == null) {
            throw new RuntimeException("This offer is not linked to a product request.");
        }

        productRequestRepository.findByIdAndClientId(productRequest.getId(), clientId)
                .orElseThrow(() -> new RuntimeException("Request not found for this client."));

        if (offer.getStatus() == ProductRequestOfferStatus.ACCEPTED) {
            throw new RuntimeException("Accepted offer cannot be rejected.");
        }

        offer.setStatus(ProductRequestOfferStatus.REJECTED);
        ProductRequestOffer saved = productRequestOfferRepository.save(offer);

        return mapOfferToResponse(saved);
    }

    @Override
    public ProductRequestResponse cancelRequest(Long clientId, Long requestId) {
        ProductRequest request = productRequestRepository.findByIdAndClientId(requestId, clientId)
                .orElseThrow(() -> new RuntimeException("Request not found for this client."));

        if (request.getStatus() == ProductRequestStatus.ACCEPTED) {
            throw new RuntimeException("Accepted request cannot be cancelled.");
        }

        request.setStatus(ProductRequestStatus.CANCELLED);
        ProductRequest saved = productRequestRepository.save(request);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductRequestResponse> getAllRequestsAdmin(ProductRequestStatus status) {
        List<ProductRequest> requests = status == null
                ? productRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : productRequestRepository.findByStatusOrderByCreatedAtDesc(status);

        return requests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductRequestResponse updateRequestStatusAdmin(Long requestId, ProductRequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }

        if (status == ProductRequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("Admin cannot force ACCEPTED. Accept an offer instead.");
        }

        ProductRequest request = productRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Product request not found with id: " + requestId));

        ProductRequestStatus currentStatus = request.getStatus();

        if (currentStatus == ProductRequestStatus.ACCEPTED
                || currentStatus == ProductRequestStatus.CANCELLED
                || currentStatus == ProductRequestStatus.CLOSED
                || currentStatus == ProductRequestStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Request with final status " + currentStatus + " cannot be changed."
            );
        }

        if (currentStatus == status) {
            throw new IllegalArgumentException("Request is already in status " + status);
        }

        request.setStatus(status);

        if (status == ProductRequestStatus.REJECTED
                || status == ProductRequestStatus.CANCELLED
                || status == ProductRequestStatus.CLOSED) {
            List<ProductRequestOffer> offers = productRequestOfferRepository.findByProductRequestId(requestId);
            for (ProductRequestOffer offer : offers) {
                if (offer.getStatus() == ProductRequestOfferStatus.PENDING) {
                    offer.setStatus(ProductRequestOfferStatus.REJECTED);
                }
            }
            productRequestOfferRepository.saveAll(offers);
        }

        ProductRequest saved = productRequestRepository.save(request);
        return mapToResponse(saved);
    }
    private ProductRequestResponse mapToResponse(ProductRequest entity) {
        String clientName = null;
        if (entity.getClient() != null) {
            clientName = buildUserDisplayName(entity.getClient());
        }

        String targetSellerName = null;
        if (entity.getTargetSeller() != null) {
            targetSellerName = buildUserDisplayName(entity.getTargetSeller());
        }

        int offersCount = entity.getOffers() != null ? entity.getOffers().size() : 0;

        return new ProductRequestResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequestedQuantity(),
                entity.getMaxBudget(),
                entity.getDesiredDate(),
                entity.getImageUrl(),
                entity.getStatus(),

                entity.getClient() != null ? entity.getClient().getId() : null,
                clientName,

                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getCategory() != null ? entity.getCategory().getName() : null,

                entity.getTargetSeller() != null ? entity.getTargetSeller().getId() : null,
                targetSellerName,

                offersCount,

                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ProductRequestOfferResponse mapOfferToResponse(ProductRequestOffer entity) {
        String sellerName = null;
        if (entity.getSeller() != null) {
            sellerName = buildUserDisplayName(entity.getSeller());
        }

        return new ProductRequestOfferResponse(
                entity.getId(),
                entity.getProductRequest() != null ? entity.getProductRequest().getId() : null,
                entity.getSeller() != null ? entity.getSeller().getId() : null,
                sellerName,
                entity.getProduct() != null ? entity.getProduct().getId() : null,
                entity.getProduct() != null ? entity.getProduct().getName() : null,
                entity.getProposedPrice(),
                entity.getProposedQuantity(),
                entity.getEstimatedDeliveryDays(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String buildUserDisplayName(User user) {
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }
}






























































































