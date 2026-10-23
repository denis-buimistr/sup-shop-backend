package com.supshop.sup_shop_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse (
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer quantity,
    String brandName,
    String categoryName,
    String boardType,
    String status,
    List<ProductImageResponse> images,
    List<ProductAttributeResponse> attributes
) {}
