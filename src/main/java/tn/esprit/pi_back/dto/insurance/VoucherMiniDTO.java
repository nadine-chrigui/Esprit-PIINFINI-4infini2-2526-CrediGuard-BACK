package tn.esprit.pi_back.dto.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VoucherMiniDTO(
        Long id,
        String code,
        BigDecimal amount,
        String status,
        LocalDate expirationDate,
        ClientDTO client
) {}