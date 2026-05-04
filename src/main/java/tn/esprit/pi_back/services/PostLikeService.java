package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.PostLike.*;
import java.util.List;

public interface PostLikeService {
    PostLikeResponse toggleLike(PostLikeRequest req); // like if not liked, unlike if already liked
    List<PostLikeResponse> getByPost(Long postId);
    long countByPost(Long postId);
    boolean hasLiked(Long userId, Long postId);
}
