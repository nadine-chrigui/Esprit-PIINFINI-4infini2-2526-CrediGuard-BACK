package tn.esprit.pi_back.services;



import tn.esprit.pi_back.dto.promocode.*;

import java.util.List;

public interface PromoCodeService {
    PromoCodeResponse create(PromoCodeCreateRequest req);
    PromoCodeResponse update(Long id, PromoCodeUpdateRequest req);
    PromoCodeResponse getById(Long id);
    List<PromoCodeResponse> getAll();
    void delete(Long id);

    PromoCodeValidateResponse validateAndCompute(PromoCodeValidateRequest req);

    void incrementUse(Long promoCodeId);
}