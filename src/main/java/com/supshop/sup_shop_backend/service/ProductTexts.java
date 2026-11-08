package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.model.Product;
import com.supshop.sup_shop_backend.model.ProductAttribute;
import com.supshop.sup_shop_backend.model.ProductAttributeTranslation;
import com.supshop.sup_shop_backend.model.ProductTranslation;

import java.util.Map;

// переводы для пачки товаров на один язык. каждое поле: перевод, если он есть, иначе английский оригинал
public record ProductTexts(
        Map<Long, ProductTranslation> products,
        Map<Long, ProductAttributeTranslation> attributes,
        CategoryTexts categories
) {

    public static final ProductTexts ORIGINAL = new ProductTexts(Map.of(), Map.of(), CategoryTexts.ORIGINAL);

    public String name(Product product) {
        ProductTranslation t = products.get(product.getId());
        return t != null && t.getName() != null ? t.getName() : product.getName();
    }

    public String description(Product product) {
        ProductTranslation t = products.get(product.getId());
        return t != null && t.getDescription() != null ? t.getDescription() : product.getDescription();
    }

    public String categoryName(Category category) {
        return category != null ? categories.name(category) : null;
    }

    public String attributeName(ProductAttribute attribute) {
        ProductAttributeTranslation t = attributes.get(attribute.getId());
        return t != null ? t.getName() : attribute.getAttributeName();
    }

    public String attributeValue(ProductAttribute attribute) {
        ProductAttributeTranslation t = attributes.get(attribute.getId());
        return t != null && t.getValue() != null ? t.getValue() : attribute.getAttributeValue();
    }
}
