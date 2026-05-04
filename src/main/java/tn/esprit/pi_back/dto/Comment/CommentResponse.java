package tn.esprit.pi_back.dto.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        String content,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long authorId,
        Long postId,
        Long parentCommentId
) {}
