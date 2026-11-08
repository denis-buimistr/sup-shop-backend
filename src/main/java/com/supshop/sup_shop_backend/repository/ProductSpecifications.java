package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Brand;
import com.supshop.sup_shop_backend.model.Category;
import com.supshop.sup_shop_backend.model.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    // режу запрос на слова, и каждое слово должно найтись в названии, бренде или категории.
    // так "fanatic fly" находит Fanatic iSUP Fly Air, хотя такой фразы целиком в названии нет
    public static Specification<Product> matchesText(String text) {
        List<String> patterns = Arrays.stream(text.toLowerCase().split("\\s+"))
                .filter(word -> !word.isBlank())
                .map(ProductSpecifications::likePattern)
                .toList();

        return (root, query, cb) -> {
            Join<Product, Brand> brand = root.join("brand", JoinType.LEFT);
            Join<Product, Category> category = root.join("category", JoinType.LEFT);
            Predicate[] words = patterns.stream()
                    .map(pattern -> cb.or(
                            cb.like(cb.lower(root.get("name")), pattern, '\\'),
                            cb.like(cb.lower(brand.get("name")), pattern, '\\'),
                            cb.like(cb.lower(category.get("name")), pattern, '\\')))
                    .toArray(Predicate[]::new);
            return cb.and(words);
        };
    }

    // % и _ экранирую, иначе пользователь сам напишет шаблон для LIKE
    private static String likePattern(String word) {
        return "%" + word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

    public static Specification<Product> hasBoardType(Product.BoardType boardType) {
        return (root, query, cb) -> cb.equal(root.get("boardType"), boardType);
    }
}
