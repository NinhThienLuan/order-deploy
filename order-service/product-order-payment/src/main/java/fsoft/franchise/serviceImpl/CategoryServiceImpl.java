package fsoft.franchise.serviceImpl;

import fsoft.franchise.dto.products.CategoryRequest;
import fsoft.franchise.dto.products.CategoryResponse;
import fsoft.franchise.entity.CategoryEntity;
import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.ProductErrorCode;
import fsoft.franchise.repository.CategoryRepository;
import fsoft.franchise.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByActiveTrueAndDeleteAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryEntity entity = CategoryEntity.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .parentId(request.getParentId())
                .active(true)
                .build();
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ProductErrorCode.CATEGORY_NOT_FOUND));
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getParentId() != null) {
            entity.setParentId(request.getParentId());
        }
        return toResponse(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ProductErrorCode.CATEGORY_NOT_FOUND));
        entity.setActive(false);
        entity.setDeleteAt(LocalDateTime.now());
        categoryRepository.save(entity);
    }

    private CategoryResponse toResponse(CategoryEntity c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .parentId(c.getParentId())
                .build();
    }
}
