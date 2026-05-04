package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Comment.*;
import tn.esprit.pi_back.entities.*;
import tn.esprit.pi_back.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BlogPostRepository blogPostRepository;

    @Override
    public CommentResponse create(CommentCreateRequest req) {
        User author = userRepository.findById(req.authorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BlogPost post = blogPostRepository.findById(req.postId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(req.content());
        comment.setAuthor(author);
        comment.setPost(post);

        if (req.parentCommentId() != null) {
            Comment parent = commentRepository.findById(req.parentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        return map(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getAll() {
        return commentRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getById(Long id) {
        return map(commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getByPost(Long postId) {
        return commentRepository.findByPostPostIdAndParentCommentIsNull(postId)
                .stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentCommentId(parentCommentId)
                .stream().map(this::map).toList();
    }

    @Override
    public CommentResponse update(Long id, CommentUpdateRequest req) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (req.content() != null) {
            comment.setContent(req.content());
            comment.setStatus(Comment.CommentStatus.EDITED);
        }
        return map(commentRepository.save(comment));
    }

    @Override
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        List<Comment> replies = commentRepository.findByParentCommentCommentId(id);
        for (Comment reply : replies) {
            commentRepository.delete(reply);
        }
        
        commentRepository.delete(comment);
    }

    private CommentResponse map(Comment c) {
        return new CommentResponse(
                c.getCommentId(),
                c.getContent(),
                c.getStatus().name(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getAuthor().getId(),
                c.getPost().getPostId(),
                c.getParentComment() != null ? c.getParentComment().getCommentId() : null
        );
    }
}
