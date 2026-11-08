package com.supshop.sup_shop_backend.dto;

import java.math.BigDecimal;
import java.util.List;

// Параметры каталога из query string: /api/products?category=sup&brand=1&brand=3&minPrice=5000&onSale=true&q=carbon
public record ProductFilter(
        String category,
        List<Long> brand,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock,
        Boolean onSale,
        String boardType,
        // строка из поиска в шапке
        String q
) {}
