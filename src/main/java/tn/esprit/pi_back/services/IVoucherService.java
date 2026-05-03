package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.insurance.VoucherMiniDTO;
import tn.esprit.pi_back.entities.Voucher;
import java.util.List;

public interface IVoucherService {
    List<VoucherMiniDTO> getAllVouchers();
    VoucherMiniDTO getVoucherById(Long id);
    VoucherMiniDTO createVoucher(Voucher voucher);
    VoucherMiniDTO updateVoucher(Long id, Voucher voucher);
    void deleteVoucher(Long id);
    VoucherMiniDTO getVoucherByCode(String code);
    VoucherMiniDTO consumeVoucher(Long voucherId, double purchaseAmount);
}