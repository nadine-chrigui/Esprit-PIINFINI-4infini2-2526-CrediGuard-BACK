package tn.esprit.pi_back.dto.Category;

import java.time.LocalDateTime;

public record CategoryResponse(

        Long id,
        String name,
        String description,
        Long parentId,
        int childrenCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}