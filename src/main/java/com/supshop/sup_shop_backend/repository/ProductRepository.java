package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);
    boolean existsBySlug(String slug);
    Optional<Product> findBySlug(String slug);

    // проверка и списание одним запросом: два параллельных заказа не уведут остаток в минус
    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :qty WHERE p.id = :id AND p.quantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :qty WHERE p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Query("SELECT p.category.id AS categoryId, COUNT(p) AS total FROM Product p " +
            "WHERE p.status = 'ACTIVE' AND p.category IS NOT NULL GROUP BY p.category.id")
    List<CategoryProductCount> countActiveByCategory();
}
