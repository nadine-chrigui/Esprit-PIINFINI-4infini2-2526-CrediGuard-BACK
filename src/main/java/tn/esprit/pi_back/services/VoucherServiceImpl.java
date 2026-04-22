package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.insurance.UserMiniDTO;
import tn.esprit.pi_back.dto.insurance.VoucherMiniDTO;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.Voucher;
import tn.esprit.pi_back.repositories.UserRepository;
import tn.esprit.pi_back.repositories.VoucherRepository;
import tn.esprit.pi_back.dto.insurance.UserMapper;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements IVoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;

    @Override
    public List<VoucherMiniDTO> getAllVouchers() {
        return voucherRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public VoucherMiniDTO getVoucherById(Long id) {
        return toDTO(voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + id)));
    }

    @Override
    public VoucherMiniDTO createVoucher(Voucher voucher) {
        // ✅ Recharger le client complet depuis la DB
        User client = userRepository.findById(voucher.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        voucher.setClient(client);
        return toDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherMiniDTO updateVoucher(Long id, Voucher voucher) {
        Voucher existing = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + id));
        existing.setCode(voucher.getCode());
        existing.setAmount(voucher.getAmount());
        existing.setStatus(voucher.getStatus());
        existing.setExpirationDate(voucher.getExpirationDate());
        // ✅ Recharger le client aussi pour l'update
        User client = userRepository.findById(voucher.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        existing.setClient(client);
        return toDTO(voucherRepository.save(existing));
    }

    @Override
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    private VoucherMiniDTO toDTO(Voucher v) {
        UserMiniDTO clientDTO = new UserMiniDTO(
                v.getClient().getId(),
                v.getClient().getFullName(),
                v.getClient().getEmail(),
                v.getClient().getPartnerType()
        );
        return new VoucherMiniDTO(
                v.getId(),
                v.getCode(),
                v.getAmount(),
                v.getStatus().name(),
                v.getExpirationDate(),
                UserMapper.toClientDTO(v.getClient())
        );
    }
    @Override
    public VoucherMiniDTO getVoucherByCode(String code) {
        Voucher v = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        return new VoucherMiniDTO(
                v.getId(),
                v.getCode(),
                v.getAmount(),
                v.getStatus().name(), // ✅ enum → String
                v.getExpirationDate(), // ✅ date
                null // ou client si dispo
        );
    }
    @Override
    public VoucherMiniDTO consumeVoucher(Long voucherId, double purchaseAmount) {

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (voucher.getAmount().doubleValue() < purchaseAmount) {
            throw new RuntimeException("Solde insuffisant ❌");
        }

        // 🔥 décrémentation
        double newAmount = voucher.getAmount().doubleValue() - purchaseAmount;
        voucher.setAmount(java.math.BigDecimal.valueOf(newAmount));

        // 🔥 status intelligent
        if (newAmount == 0) {
            voucher.setStatus(tn.esprit.pi_back.entities.enums.VoucherStatus.USED);
        } else {
            voucher.setStatus(tn.esprit.pi_back.entities.enums.VoucherStatus.ACTIVE);
        }

        voucherRepository.save(voucher);

        return toDTO(voucher);
    }

}