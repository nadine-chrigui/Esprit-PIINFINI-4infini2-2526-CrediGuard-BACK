package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.checkout.CheckoutRequest;
import tn.esprit.pi_back.dto.checkout.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse checkout(CheckoutRequest req);
}