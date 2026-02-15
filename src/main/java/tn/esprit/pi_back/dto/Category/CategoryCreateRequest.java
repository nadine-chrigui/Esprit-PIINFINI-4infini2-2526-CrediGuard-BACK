package tn.esprit.pi_back.dto.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(

        @NotBlank(message = "name is required")
        @Size(min = 2, max = 60, message = "name must be between 2 and 60 characters")
        String name,

        @Size(max = 255, message = "description must be <= 255 characters")
        String description,

        Long parentId

) {}