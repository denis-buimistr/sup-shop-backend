package com.supshop.sup_shop_backend.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse (
        Long id,
        Long productId,
        String productName,
        // текущее главное фото товара, может быть null, если фото нет
        String imageUrl,
        Integer quantity,
        BigDecimal priceAtPurchase
) {}

