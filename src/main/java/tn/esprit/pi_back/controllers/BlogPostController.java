package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.BlogPost.*;
import tn.esprit.pi_back.services.BlogPostService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/blogposts")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BlogPostController {

    private final BlogPostService blogPostService;

    @PostMapping
    public ResponseEntity<BlogPostResponse> create(@Valid @RequestBody BlogPostCreateRequest req) {
        BlogPostResponse created = blogPostService.create(req);
        return ResponseEntity.created(URI.create("/api/blogposts/" + created.id())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BlogPostResponse>> getAll() {
        return ResponseEntity.ok(blogPostService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogPostResponse> getById(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(blogPostService.getById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogPostResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody BlogPostUpdateRequest req) {
        return ResponseEntity.ok(blogPostService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blogPostService.delete(id);
        return ResponseEntity.noContent().build();
    }
}