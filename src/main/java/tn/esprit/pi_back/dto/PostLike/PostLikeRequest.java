package tn.esprit.pi_back.dto.PostLike;

import jakarta.validation.constraints.NotNull;

public record PostLikeRequest(
        @NotNull Long userId,
        @NotNull Long postId
) {}
