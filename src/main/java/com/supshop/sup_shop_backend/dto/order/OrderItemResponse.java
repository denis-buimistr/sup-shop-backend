package com.supshop.sup_shop_backend.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse (
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtPurchase
) {}

