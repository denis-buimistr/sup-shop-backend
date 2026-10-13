package com.supshop.sup_shop_backend.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {}