package tn.esprit.pi_back.mappers;

import org.springframework.stereotype.Component;
import tn.esprit.pi_back.dto.Category.CategoryCreateRequest;
import tn.esprit.pi_back.dto.Category.CategoryResponse;
import tn.esprit.pi_back.dto.Category.CategoryUpdateRequest;
import tn.esprit.pi_back.entities.Category;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryCreateRequest req, Category parent) {
        Category category = new Category();

        category.setName(req.name().trim());
        category.setDescription(req.description());
        category.setParent(parent);

        return category;
    }

    public void updateEntity(Category category, CategoryUpdateRequest req, Category parent) {
        if (req.name() != null && !req.name().trim().isBlank()) {
            category.setName(req.name().trim());
        }

        if (req.description() != null) {
            category.setDescription(req.description());
        }

        if (req.parentId() != null) {
            category.setParent(parent);
        }
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren() != null ? category.getChildren().size() : 0,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}