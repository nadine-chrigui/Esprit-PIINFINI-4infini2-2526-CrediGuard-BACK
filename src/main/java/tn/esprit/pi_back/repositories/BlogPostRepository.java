package tn.esprit.pi_back.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pi_back.entities.BlogPost;

import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByAuthorId(Long authorId);
    List<BlogPost> findByProjectProjectId(Long projectId);
    List<BlogPost> findByStatus(BlogPost.PostStatus status);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE BlogPost b SET b.viewCount = COALESCE(b.viewCount, 0) + 1 WHERE b.postId = :id")
    void incrementViewCount(@org.springframework.data.repository.query.Param("id") Long id);
}
