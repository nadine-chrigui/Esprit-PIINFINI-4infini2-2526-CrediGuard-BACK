package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.PostLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserIdAndPostPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostPostId(Long userId, Long postId);
    List<PostLike> findByPostPostId(Long postId);
    long countByPostPostId(Long postId);
}
