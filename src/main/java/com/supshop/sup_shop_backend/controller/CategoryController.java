package com.supshop.sup_shop_backend.controller;

import com.supshop.sup_shop_backend.dto.CategoryRequest;
import com.supshop.sup_shop_backend.dto.CategoryResponse;
import com.supshop.sup_shop_backend.service.CategoryService;
import com.supshop.sup_shop_backend.service.TranslationService;
import com.supshop.sup_shop_backend.dto.TranslationResponse;
import com.supshop.sup_shop_backend.dto.TranslationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final TranslationService translationService;

    public CategoryController(CategoryService categoryService, TranslationService translationService) {
        this.categoryService = categoryService;
        this.translationService = translationService;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    // переводы на ro и ru для админки. GET открыт вместе со всем каталогом, PUT только админу (SecurityConfig)
    @GetMapping("/{id}/translations")
    public List<TranslationResponse> getTranslations(@PathVariable Long id) {
        return translationService.forCategory(id);
    }

    @PutMapping("/{id}/translations/{lang}")
    public List<TranslationResponse> saveTranslation(@PathVariable Long id, @PathVariable String lang,
                                                     @Valid @RequestBody TranslationRequest request) {
        return translationService.saveForCategory(id, lang, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
