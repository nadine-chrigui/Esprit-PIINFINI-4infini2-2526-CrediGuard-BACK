package tn.esprit.pi_back.dto.BlogPost;

import java.time.LocalDateTime;

public record BlogPostResponse(

        Long id,
        String title,
        String content,
        String status,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long authorId,
        Long projectId

) {}