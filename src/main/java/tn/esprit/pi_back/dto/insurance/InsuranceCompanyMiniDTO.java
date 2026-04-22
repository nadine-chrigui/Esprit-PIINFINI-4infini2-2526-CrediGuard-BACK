package tn.esprit.pi_back.dto.insurance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsuranceCompanyMiniDTO {
    private Long id;
    private String name;
}