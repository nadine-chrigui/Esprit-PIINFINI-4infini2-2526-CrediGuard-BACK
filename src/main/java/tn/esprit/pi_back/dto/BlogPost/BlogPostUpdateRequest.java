package tn.esprit.pi_back.dto.BlogPost;

import jakarta.validation.constraints.Size;

public record BlogPostUpdateRequest(

        @Size(min = 3, max = 150)
        String title,

        @Size(min = 10, max = 5000)
        String content

) {}
