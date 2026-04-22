package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.Category.CategoryCreateRequest;
import tn.esprit.pi_back.dto.Category.CategoryResponse;
import tn.esprit.pi_back.dto.Category.CategoryUpdateRequest;
import tn.esprit.pi_back.entities.Category;
import tn.esprit.pi_back.mappers.CategoryMapper;
import tn.esprit.pi_back.repositories.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryCreateRequest req) {
        String name = req.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Category already exists");
        }

        Category parent = null;
        if (req.parentId() != null) {
            parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
        }

        Category category = categoryMapper.toEntity(req, parent);
        Category saved = categoryRepository.save(category);

        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Category parent = null;
        if (req.parentId() != null) {
            if (req.parentId().equals(id)) {
                throw new RuntimeException("A category cannot be its own parent");
            }

            parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
        }

        categoryMapper.updateEntity(category, req, parent);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}