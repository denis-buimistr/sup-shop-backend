package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    // главное фото, а если его не отметили — первое по порядку
    Optional<ProductImage> findFirstByProductIdOrderByPrimaryDescSortOrderAsc(Long productId);
}