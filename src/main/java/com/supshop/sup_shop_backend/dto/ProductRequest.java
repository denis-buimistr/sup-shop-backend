package com.supshop.sup_shop_backend.dto;

import java.math.BigDecimal;

public record ProductRequest (
    String name,
    String description,
    BigDecimal price,
    Integer quantity,
    Long brandId,
    Long categoryId,
    String boardType
) {}
