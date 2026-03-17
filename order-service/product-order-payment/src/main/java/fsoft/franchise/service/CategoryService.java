package fsoft.franchise.service;

import fsoft.franchise.dto.products.CategoryRequest;
import fsoft.franchise.dto.products.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Category management — read + admin write operations.
 */
public interface CategoryService {
    /** Returns all active, non-deleted categories. */
    List<CategoryResponse> getCategories();

    /** Creates a new category and returns the persisted record. */
    CategoryResponse createCategory(CategoryRequest request);

    /** Updates an existing category by ID. */
    CategoryResponse updateCategory(UUID id, CategoryRequest request);

    /** Soft-deletes a category by ID. */
    void deleteCategory(UUID id);
}
