package com.supshop.sup_shop_backend.dto;

public record BrandResponse (
    Long id,
    String name,
    String logoUrl,
    String description
) {}

