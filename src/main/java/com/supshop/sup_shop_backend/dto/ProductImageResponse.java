package com.supshop.sup_shop_backend.dto;

public record ProductImageResponse(
        Long id,
        String imageUrl,
        boolean primary,
        Integer sortOrder
) {}