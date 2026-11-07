package com.supshop.sup_shop_backend.repository;

// Строка результата GROUP BY: сколько активных товаров лежит прямо в категории
public interface CategoryProductCount {
    Long getCategoryId();
    long getTotal();
}
