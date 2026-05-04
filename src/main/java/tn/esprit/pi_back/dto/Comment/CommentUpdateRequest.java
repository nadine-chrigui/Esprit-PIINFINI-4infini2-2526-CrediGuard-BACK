package tn.esprit.pi_back.dto.Comment;

import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @Size(min = 2, max = 2000)
        String content
) {}
