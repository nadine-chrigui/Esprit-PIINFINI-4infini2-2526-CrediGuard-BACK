package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.product.*;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductCreateRequest req);
    List<ProductResponse> getAll();
    ProductResponse getById(Long id);
    List<ProductResponse> getMine();
    ProductResponse update(Long id, ProductUpdateRequest req);
    void delete(Long id);
    List<ProductResponse> getBySellerId(Long sellerId);
}