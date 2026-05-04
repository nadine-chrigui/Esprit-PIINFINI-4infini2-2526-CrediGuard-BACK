package tn.esprit.pi_back.dto.PostLike;

import java.time.LocalDateTime;

public record PostLikeResponse(
        Long likeId,
        Long userId,
        Long postId,
        LocalDateTime createdAt
) {}
