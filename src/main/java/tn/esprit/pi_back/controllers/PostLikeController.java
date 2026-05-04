package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.PostLike.*;
import tn.esprit.pi_back.services.PostLikeService;

import java.util.List;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/toggle")
    public ResponseEntity<PostLikeResponse> toggle(@Valid @RequestBody PostLikeRequest req) {
        PostLikeResponse result = postLikeService.toggleLike(req);
        if (result == null) return ResponseEntity.noContent().build(); // unliked
        return ResponseEntity.ok(result);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostLikeResponse>> getByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postLikeService.getByPost(postId));
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> count(@PathVariable Long postId) {
        return ResponseEntity.ok(postLikeService.countByPost(postId));
    }

    @GetMapping("/post/{postId}/user/{userId}")
    public ResponseEntity<Boolean> hasLiked(@PathVariable Long postId, @PathVariable Long userId) {
        return ResponseEntity.ok(postLikeService.hasLiked(userId, postId));
    }
}
