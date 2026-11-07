package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collection;

// Кусочки WHERE для каталога: сервис собирает из них запрос под выбранные фильтры
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), "ACTIVE");
    }

    public static Specification<Product> inCategories(Collection<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Product> hasBrands(Collection<Long> brandIds) {
        return (root, query, cb) -> root.get("brand").get("id").in(brandIds);
    }

    public static Specification<Product> priceFrom(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> priceTo(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), max);
    }

    public static Specification<Product> inStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("quantity"), 0);
    }

    public static Specification<Product> onSale() {
        return (root, query, cb) -> cb.isNotNull(root.get("compareAtPrice"));
    }

    public static Specification<Product> hasBoardType(Product.BoardType boardType) {
        return (root, query, cb) -> cb.equal(root.get("boardType"), boardType);
    }
}
