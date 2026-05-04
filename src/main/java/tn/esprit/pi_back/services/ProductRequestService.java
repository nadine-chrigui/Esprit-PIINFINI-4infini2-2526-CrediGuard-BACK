package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.productrequest.ProductRequestCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferCreateRequest;
import tn.esprit.pi_back.dto.productrequest.ProductRequestOfferResponse;
import tn.esprit.pi_back.dto.productrequest.ProductRequestResponse;
import tn.esprit.pi_back.entities.enums.ProductRequestStatus;

import java.util.List;

public interface ProductRequestService {

    ProductRequestResponse createRequest(Long clientId, ProductRequestCreateRequest request);

    List<ProductRequestResponse> getMyRequests(Long clientId);

    List<ProductRequestResponse> getOpenRequestsForSellers();

    List<ProductRequestResponse> getRequestsForTargetSeller(Long sellerId);

    ProductRequestOfferResponse createOffer(Long sellerId, Long productRequestId, ProductRequestOfferCreateRequest request);

    List<ProductRequestOfferResponse> getOffersForRequest(Long productRequestId);

    List<ProductRequestOfferResponse> getMyOffers(Long sellerId);

    ProductRequestOfferResponse acceptOffer(Long clientId, Long offerId);

    ProductRequestOfferResponse rejectOffer(Long clientId, Long offerId);

    ProductRequestResponse getRequestById(Long requestId);
    ProductRequestResponse cancelRequest(Long clientId, Long requestId);
    List<ProductRequestResponse> getAllRequestsAdmin(ProductRequestStatus status);
    ProductRequestResponse updateRequestStatusAdmin(Long requestId, ProductRequestStatus status);

}
