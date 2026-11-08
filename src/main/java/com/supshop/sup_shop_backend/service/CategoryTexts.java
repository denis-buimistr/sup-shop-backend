package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.model.CategoryTranslation;

import java.util.Map;

// переводы категорий на один язык. нет перевода — отдаю английский оригинал
public record CategoryTexts(Map<Long, CategoryTranslation> byCategoryId) {

    public static final CategoryTexts ORIGINAL = new CategoryTexts(Map.of());

    public String name(Category category) {
        CategoryTranslation t = byCategoryId.get(category.getId());
        return t != null ? t.getName() : category.getName();
    }

    public String description(Category category) {
        CategoryTranslation t = byCategoryId.get(category.getId());
        return t != null && t.getDescription() != null ? t.getDescription() : category.getDescription();
    }
}
