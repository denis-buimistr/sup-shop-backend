package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository <Category, Long> {
}
