package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.BlogPost.*;
import java.util.List;

public interface BlogPostService {
    BlogPostResponse create(BlogPostCreateRequest req);
    List<BlogPostResponse> getAll();
    BlogPostResponse getById(Long id, Long userId);
    BlogPostResponse update(Long id, BlogPostUpdateRequest req);
    void delete(Long id);
}
