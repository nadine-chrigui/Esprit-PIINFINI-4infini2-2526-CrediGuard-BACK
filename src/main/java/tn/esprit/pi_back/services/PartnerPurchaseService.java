package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.entities.PartnerPurchase;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.Voucher;
import tn.esprit.pi_back.repositories.PartnerPurchaseRepository;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.repositories.VoucherRepository;
import tn.esprit.pi_back.dto.partnership.PartnerPurchaseDTO;
import tn.esprit.pi_back.dto.partnership.CreatePurchaseRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerPurchaseService {

    private final PartnerPurchaseRepository purchaseRepo;
    private final UserRepository userRepo;
    private final VoucherRepository voucherRepo;

    public PartnerPurchaseDTO createPurchase(CreatePurchaseRequest req) {
        User client = userRepo.findById(req.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        User partner = userRepo.findById(req.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        Voucher voucher = voucherRepo.findById(req.getVoucherId())
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        PartnerPurchase p = PartnerPurchase.builder()
                .purchaseReference("PURCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .totalAmount(req.getTotalAmount())
                .productNames(req.getProductNames())
                .client(client)
                .partner(partner)
                .voucher(voucher)
                .build();

        return mapToDTO(purchaseRepo.save(p));
    }

    public List<PartnerPurchaseDTO> getByClient(Long clientId) {
        return purchaseRepo.findByClientId(clientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PartnerPurchaseDTO> getAll() {
        return purchaseRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PartnerPurchaseDTO mapToDTO(PartnerPurchase p) {
        return PartnerPurchaseDTO.builder()
                .id(p.getId())
                .purchaseReference(p.getPurchaseReference())
                .totalAmount(p.getTotalAmount())
                .productNames(p.getProductNames())
                .createdAt(p.getCreatedAt())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientName(p.getClient() != null ? p.getClient().getFullName() : null)
                .partnerId(p.getPartner() != null ? p.getPartner().getId() : null)
                .partnerName(p.getPartner() != null ? p.getPartner().getFullName() : null)
                .voucherCode(p.getVoucher() != null ? p.getVoucher().getCode() : null)
                .build();
    }
}
