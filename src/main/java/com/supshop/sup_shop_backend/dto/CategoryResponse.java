package com.supshop.sup_shop_backend.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentId
) {}