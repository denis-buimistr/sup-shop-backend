package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByBrandId(Long brandId, Pageable pageable);
    Page<Product> findByBoardType(Product.BoardType boardType, Pageable pageable);
    Page<Product> findByStatus(String status, Pageable pageable);
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
    Page<Product> findByBrandIdAndStatus(Long brandId, String status, Pageable pageable);
    Page<Product> findByBoardTypeAndStatus(Product.BoardType boardType, String status, Pageable pageable);
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
}
