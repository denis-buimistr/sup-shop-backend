package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.CategoryRequest;
import com.supshop.sup_shop_backend.dto.CategoryResponse;
import com.supshop.sup_shop_backend.exception.ConflictException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supshop.sup_shop_backend.util.SlugUtil;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists: " + name);
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(SlugUtil.uniqueSlug(name, "category", categoryRepository::existsBySlug));
        category.setDescription(request.description());
        category.setParent(findParent(request.parentId()));

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findById(id);

        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Category already exists: " + name);
        }

        Category parent = findParent(request.parentId());
        if (isSelfOrDescendant(parent, id)) {
            throw new IllegalArgumentException("Category can't be moved inside itself");
        }

        category.setName(name);
        category.setDescription(request.description());
        category.setParent(parent);
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found: " + id);
        }
        if (categoryRepository.existsByParentId(id)) {
            throw new ConflictException("Category has subcategories and can't be deleted");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category has products and can't be deleted");
        }
        categoryRepository.deleteById(id);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: " + id));
    }

    private Category findParent(Long parentId) {
        return parentId == null ? null : findById(parentId);
    }

    // идём от нового родителя вверх по дереву: если встретили саму категорию — получится цикл
    private boolean isSelfOrDescendant(Category candidate, Long categoryId) {
        for (Category current = candidate; current != null; current = current.getParent()) {
            if (current.getId().equals(categoryId)) {
                return true;
            }
        }
        return false;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }
}
