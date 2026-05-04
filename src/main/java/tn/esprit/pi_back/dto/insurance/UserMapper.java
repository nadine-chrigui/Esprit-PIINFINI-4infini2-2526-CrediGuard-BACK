package tn.esprit.pi_back.dto.insurance;

import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.UserType;

public class UserMapper {

    // 🔥 CLIENT
    public static ClientDTO toClientDTO(User u) {
        if (u == null) return null;
        return new ClientDTO(
                u.getId(),
                u.getFullName(),
                u.getEmail()
        );
    }

    // 🔥 PARTNER
    public static PartnerDTO toPartnerDTO(User u) {
        if (u == null) return null;
        return new PartnerDTO(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPartnerType()
        );
    }

    // 🔥 GENERIC (optionnel)
    public static UserMiniDTO toUserMiniDTO(User u) {
        if (u == null) return null;
        return new UserMiniDTO(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPartnerType()
        );
    }

    // 🔥 SMART MAPPING (BONUS PRO)
    public static Object toDTO(User u) {
        if (u == null) return null;
        if (u.getUserType() == null) return toClientDTO(u); // Default fallback
        
        return switch (u.getUserType()) {
            case CLIENT -> toClientDTO(u);
            case PARTNER -> toPartnerDTO(u);
            case INSURANCE -> toUserMiniDTO(u);
            default -> toUserMiniDTO(u);
        };
    }
}