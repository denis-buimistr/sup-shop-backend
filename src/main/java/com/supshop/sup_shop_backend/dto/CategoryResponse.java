package com.supshop.sup_shop_backend.dto;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        // активные товары в категории вместе со всеми подкатегориями
        long productCount
) {}