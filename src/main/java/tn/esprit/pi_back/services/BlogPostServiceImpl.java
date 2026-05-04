package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.BlogPost.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogPostServiceImpl implements BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final CrowdfundingProjectRepository projectRepository;

    @Override
    public BlogPostResponse create(BlogPostCreateRequest req) {

        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CrowdfundingProject project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        BlogPost post = new BlogPost();
        post.setTitle(req.title().trim());
        post.setContent(req.content());
        post.setAuthor(author);
        post.setProject(project);
        post.setStatus(BlogPost.PostStatus.PUBLISHED);

        BlogPost saved = blogPostRepository.save(post);

        return mapToResponse(saved);
    }

    @Override
    public List<BlogPostResponse> getAll() {
        return blogPostRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BlogPostResponse getById(Long id, Long userId) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean incremented = false;
        if (userId != null) {
            if (!post.getViewers().contains(userId)) {
                post.getViewers().add(userId);
                incremented = true;
            }
        } else {
            incremented = true; // anonymous users increment always (or track by IP if needed later)
        }

        if (incremented) {
            post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
            blogPostRepository.save(post);
        }

        return mapToResponse(post);
    }

    @Override
    public BlogPostResponse update(Long id, BlogPostUpdateRequest req) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (req.title() != null) {
            post.setTitle(req.title().trim());
        }

        if (req.content() != null) {
            post.setContent(req.content());
        }

        return mapToResponse(post);
    }

    @Override
    public void delete(Long id) {
        blogPostRepository.deleteById(id);
    }

    private BlogPostResponse mapToResponse(BlogPost p) {
        return new BlogPostResponse(
                p.getPostId(),
                p.getTitle(),
                p.getContent(),
                p.getStatus().name(),
                p.getViewCount(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getAuthor().getId(),
                p.getProject().getProjectId()
        );
    }
}