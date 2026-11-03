package com.supshop.sup_shop_backend.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public record OrderResponse (
    Long id,
    String status,
    String customerUsername,
    String customerEmail,
    BigDecimal totalPrice,
    String shippingAddress,
    List<OrderItemResponse> items,
    LocalDateTime createdAt
) {}


