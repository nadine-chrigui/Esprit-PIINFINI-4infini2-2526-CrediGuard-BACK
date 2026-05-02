package tn.esprit.pi_back.entities.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PolicyStatusConverter implements AttributeConverter<PolicyStatus, String> {

    @Override
    public String convertToDatabaseColumn(PolicyStatus attribute) {
        if (attribute == null) {
            return PolicyStatus.PENDING.name();
        }
        return attribute.name();
    }

    @Override
    public PolicyStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return PolicyStatus.PENDING;
        }
        try {
            return PolicyStatus.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle common variants manually if needed, or just return PENDING
            if (dbData.equalsIgnoreCase("ACTIVE")) return PolicyStatus.ACTIF;
            if (dbData.equalsIgnoreCase("EXPIRED")) return PolicyStatus.EXPIRE;
            if (dbData.equalsIgnoreCase("SUSPENDED")) return PolicyStatus.SUSPENDU;
            if (dbData.equalsIgnoreCase("CANCELED")) return PolicyStatus.RESILIE;
            
            return PolicyStatus.PENDING; // Fallback for any unknown status
        }
    }
}
