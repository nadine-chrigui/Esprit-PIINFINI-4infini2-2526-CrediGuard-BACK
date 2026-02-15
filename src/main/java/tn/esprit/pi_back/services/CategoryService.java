package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.Category.*;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryCreateRequest req);

    List<CategoryResponse> getAll();

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, CategoryUpdateRequest req);

    void delete(Long id);
}