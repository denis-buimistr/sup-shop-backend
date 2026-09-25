package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findByProductId(Long productId);
}