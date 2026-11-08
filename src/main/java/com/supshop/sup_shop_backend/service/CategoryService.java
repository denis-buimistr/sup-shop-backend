package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.dto.CategoryRequest;
import com.supshop.sup_shop_backend.dto.CategoryResponse;
import com.supshop.sup_shop_backend.exception.ConflictException;
import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.exception.NotFoundException;
import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.repository.CategoryProductCount;
import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supshop.sup_shop_backend.util.SlugUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final TranslationService translationService;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           TranslationService translationService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.translationService = translationService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        List<Category> categories = categoryRepository.findAll(Sort.by("name"));
        Map<Long, Long> counts = countProductsWithSubcategories(categories);
        CategoryTexts texts = translationService.categories();
        return categories.stream()
                .map(c -> toResponse(c, counts.getOrDefault(c.getId(), 0L), texts))
                .toList();
    }

    // id категории и всех её потомков — каталог «SUP» должен показывать товары из всех подкатегорий SUP
    @Transactional(readOnly = true)
    public Set<Long> findSelfAndDescendantIds(String slug) {
        List<Category> categories = categoryRepository.findAll();
        Category root = categories.stream()
                .filter(c -> c.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("error.category.notFound"));

        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Category c : categories) {
            if (c.getParent() != null) {
                childrenByParent.computeIfAbsent(c.getParent().getId(), k -> new ArrayList<>()).add(c.getId());
            }
        }

        Set<Long> ids = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>(List.of(root.getId()));
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            ids.add(id);
            queue.addAll(childrenByParent.getOrDefault(id, List.of()));
        }
        return ids;
    }

    // товары лежат в листьях, а в меню у «SUP» нужна сумма по всем подкатегориям — поднимаем счётчик к родителям
    private Map<Long, Long> countProductsWithSubcategories(List<Category> categories) {
        Map<Long, Long> parentOf = new HashMap<>();
        for (Category c : categories) {
            parentOf.put(c.getId(), c.getParent() != null ? c.getParent().getId() : null);
        }

        Map<Long, Long> totals = new HashMap<>();
        for (CategoryProductCount row : productRepository.countActiveByCategory()) {
            for (Long id = row.getCategoryId(); id != null; id = parentOf.get(id)) {
                totals.merge(id, row.getTotal(), Long::sum);
            }
        }
        return totals;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("error.category.exists", name);
        }

        Category category = new Category();
        category.setName(name);
        category.setSlug(SlugUtil.uniqueSlug(name, "category", categoryRepository::existsBySlug));
        category.setDescription(request.description());
        category.setParent(findParent(request.parentId()));

        return toResponse(categoryRepository.save(category), 0L, translationService.categories());
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findById(id);

        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("error.category.exists", name);
        }

        Category parent = findParent(request.parentId());
        if (isSelfOrDescendant(parent, id)) {
            throw new BadRequestException("error.category.cycle");
        }

        category.setName(name);
        category.setDescription(request.description());
        category.setParent(parent);
        long count = countProductsWithSubcategories(categoryRepository.findAll()).getOrDefault(id, 0L);
        return toResponse(category, count, translationService.categories());
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("error.category.notFound");
        }
        if (categoryRepository.existsByParentId(id)) {
            throw new ConflictException("error.category.hasChildren");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("error.category.hasProducts");
        }
        categoryRepository.deleteById(id);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.category.notFound"));
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

    private CategoryResponse toResponse(Category category, long productCount, CategoryTexts texts) {
        return new CategoryResponse(
                category.getId(),
                texts.name(category),
                category.getSlug(),
                texts.description(category),
                category.getParent() != null ? category.getParent().getId() : null,
                productCount
        );
    }
}
