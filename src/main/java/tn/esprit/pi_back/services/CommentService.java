package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Comment.*;
import java.util.List;

public interface CommentService {
    CommentResponse create(CommentCreateRequest req);
    List<CommentResponse> getAll();
    CommentResponse getById(Long id);
    List<CommentResponse> getByPost(Long postId);
    List<CommentResponse> getReplies(Long parentCommentId);
    CommentResponse update(Long id, CommentUpdateRequest req);
    void delete(Long id);
}
