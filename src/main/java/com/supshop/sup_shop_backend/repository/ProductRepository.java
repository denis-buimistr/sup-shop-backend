package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByBrandId(Long brandId, Pageable pageable);
    Page<Product> findByBoardType(Product.BoardType boardType, Pageable pageable);
    Page<Product> findByStatus(String status, Pageable pageable);
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
    Page<Product> findByBrandIdAndStatus(Long brandId, String status, Pageable pageable);
    Page<Product> findByBoardTypeAndStatus(Product.BoardType boardType, String status, Pageable pageable);
}