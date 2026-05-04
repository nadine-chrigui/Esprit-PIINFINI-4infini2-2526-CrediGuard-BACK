package tn.esprit.pi_back.dto.Comment;

import jakarta.validation.constraints.*;

public record CommentCreateRequest(

        @NotBlank @Size(min = 2, max = 2000)
        String content,

        @NotNull
        Long authorId,

        @NotNull
        Long postId,

        Long parentCommentId   // null for top-level comments

) {}
