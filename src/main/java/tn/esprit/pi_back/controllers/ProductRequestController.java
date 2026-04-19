package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.productrequest.ProductRequestCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferResponse;
import tn.esprit.pi_back.dto.productrequest.ProductRequestResponse;
import tn.esprit.pi_back.entities.enums.ProductRequestStatus;
import tn.esprit.pi_back.services.ProductRequestService;

import java.util.List;

@RestController
@RequestMapping("/product-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductRequestController {

    private final ProductRequestService productRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductRequestResponse createRequest(
            @RequestParam Long clientId,
            @Valid @RequestBody ProductRequestCreateRequest request
    ) {
        return productRequestService.createRequest(clientId, request);
    }

    @GetMapping("/my")
    public List<ProductRequestResponse> getMyRequests(@RequestParam Long clientId) {
        return productRequestService.getMyRequests(clientId);
    }

    @GetMapping("/open")
    public List<ProductRequestResponse> getOpenRequestsForSellers() {
        return productRequestService.getOpenRequestsForSellers();
    }

    @GetMapping("/target-seller")
    public List<ProductRequestResponse> getRequestsForTargetSeller(@RequestParam Long sellerId) {
        return productRequestService.getRequestsForTargetSeller(sellerId);
    }

    @GetMapping("/{requestId}")
    public ProductRequestResponse getRequestById(@PathVariable Long requestId) {
        return productRequestService.getRequestById(requestId);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductRequestResponse> getAllRequestsAdmin(
            @RequestParam(required = false) ProductRequestStatus status
    ) {
        return productRequestService.getAllRequestsAdmin(status);
    }

    @PatchMapping("/admin/{requestId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductRequestResponse updateRequestStatusAdmin(
            @PathVariable Long requestId,
            @RequestParam ProductRequestStatus status
    ) {
        return productRequestService.updateRequestStatusAdmin(requestId, status);
    }

    @PostMapping("/{requestId}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductRequestOfferResponse createOffer(
            @PathVariable Long requestId,
            @RequestParam Long sellerId,
            @Valid @RequestBody ProductRequestOfferCreateRequest request
    ) {
        return productRequestService.createOffer(sellerId, requestId, request);
    }

    @GetMapping("/{requestId}/offers")
    public List<ProductRequestOfferResponse> getOffersForRequest(@PathVariable Long requestId) {
        return productRequestService.getOffersForRequest(requestId);
    }

    @GetMapping("/offers/my")
    public List<ProductRequestOfferResponse> getMyOffers(@RequestParam Long sellerId) {
        return productRequestService.getMyOffers(sellerId);
    }

    @PostMapping("/offers/{offerId}/accept")
    public ProductRequestOfferResponse acceptOffer(
            @PathVariable Long offerId,
            @RequestParam Long clientId
    ) {
        return productRequestService.acceptOffer(clientId, offerId);
    }

    @PostMapping("/offers/{offerId}/reject")
    public ProductRequestOfferResponse rejectOffer(
            @PathVariable Long offerId,
            @RequestParam Long clientId
    ) {
        return productRequestService.rejectOffer(clientId, offerId);
    }

    @PostMapping("/{requestId}/cancel")
    public ProductRequestResponse cancelRequest(
            @PathVariable Long requestId,
            @RequestParam Long clientId
    ) {
        return productRequestService.cancelRequest(clientId, requestId);
    }
    @GetMapping("/admin/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductRequestResponse getRequestByIdAdmin(@PathVariable Long requestId) {
        return productRequestService.getRequestById(requestId);
    }
    @GetMapping("/admin/{requestId}/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductRequestOfferResponse> getOffersForRequestAdmin(@PathVariable Long requestId) {
        return productRequestService.getOffersForRequest(requestId);
    }
}
