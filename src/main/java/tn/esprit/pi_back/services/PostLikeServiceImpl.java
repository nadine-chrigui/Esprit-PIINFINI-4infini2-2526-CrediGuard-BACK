package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.PostLike.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final BlogPostRepository blogPostRepository;

    @Override
    public PostLikeResponse toggleLike(PostLikeRequest req) {
        Optional<PostLike> existing = postLikeRepository.findByUserIdAndPostPostId(req.userId(), req.postId());

        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return null; // unliked
        }

        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        BlogPost post = blogPostRepository.findById(req.postId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostLike like = new PostLike();
        like.setUser(user);
        like.setPost(post);

        return map(postLikeRepository.save(like));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostLikeResponse> getByPost(Long postId) {
        return postLikeRepository.findByPostPostId(postId).stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByPost(Long postId) {
        return postLikeRepository.countByPostPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasLiked(Long userId, Long postId) {
        return postLikeRepository.existsByUserIdAndPostPostId(userId, postId);
    }

    private PostLikeResponse map(PostLike l) {
        return new PostLikeResponse(
                l.getLikeId(),
                l.getUser().getId(),
                l.getPost().getPostId(),
                l.getCreatedAt()
        );
    }
}
