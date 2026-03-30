package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Category.CategoryResponse;
import tn.esprit.pi_back.dto.Category.*;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CategoryCreateRequest req) {
        String name = req.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(req.description());

        if (req.parentId() != null) {
            Category parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (req.name() != null && !req.name().trim().isBlank()) {
            category.setName(req.name().trim());
        }

        if (req.description() != null) {
            category.setDescription(req.description());
        }

        if (req.parentId() != null) {
            if (req.parentId().equals(id)) {
                throw new RuntimeException("A category cannot be its own parent");
            }

            Category parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));

            category.setParent(parent);
        }

        return mapToResponse(category);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getChildren() != null ? c.getChildren().size() : 0,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}