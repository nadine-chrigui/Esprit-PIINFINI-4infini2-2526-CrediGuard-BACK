package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostPostId(Long postId);
    List<Comment> findByAuthorId(Long authorId);
    List<Comment> findByPostPostIdAndParentCommentIsNull(Long postId);
    List<Comment> findByParentCommentCommentId(Long parentCommentId);
}
