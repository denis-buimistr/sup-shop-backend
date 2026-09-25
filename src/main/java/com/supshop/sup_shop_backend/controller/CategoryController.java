package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.repository.CategoryRepository;
import com.supshop.sup_shop_backend.dto.CategoryResponse;
import com.supshop.sup_shop_backend.model.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getDescription(),
                        c.getParent() != null ? c.getParent().getId() : null
                ))
                .toList();
    }
}
