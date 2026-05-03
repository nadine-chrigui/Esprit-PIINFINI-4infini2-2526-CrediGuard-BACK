package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.insurance.VoucherMiniDTO;
import tn.esprit.pi_back.entities.Voucher;
import tn.esprit.pi_back.services.IVoucherService;
import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class VoucherController {

    private final IVoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherMiniDTO>> getAll() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherMiniDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<VoucherMiniDTO> create(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherMiniDTO> update(@PathVariable Long id, @RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/code/{code}")
    public ResponseEntity<VoucherMiniDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }
    @GetMapping("/test")
    public String test() {
        return "VOUCHER OK";
    }
    @PutMapping("/consume/{id}")
    public VoucherMiniDTO consume(@PathVariable Long id, @RequestParam double amount) {
        return voucherService.consumeVoucher(id, amount);
    }

}