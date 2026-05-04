package tn.esprit.pi_back.dto.BlogPost;

import jakarta.validation.constraints.*;

public record BlogPostCreateRequest(

        @NotBlank
        @Size(min = 3, max = 150)
        String title,

        @NotBlank
        @Size(min = 10, max = 5000)
        String content,

        Long authorId,
        Long projectId

) {}