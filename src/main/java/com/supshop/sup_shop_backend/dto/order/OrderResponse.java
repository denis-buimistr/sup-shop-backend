package com.supshop.sup_shop_backend.dto.order;

import com.supshop.sup_shop_backend.model.DeliveryMethod;
import com.supshop.sup_shop_backend.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String status,
        String customerUsername,
        String customerEmail,
        String firstName,
        String lastName,
        String phone,
        String email,
        DeliveryMethod deliveryMethod,
        String city,
        String shippingAddress,
        BigDecimal deliveryPrice,
        PaymentMethod paymentMethod,
        String comment,
        BigDecimal totalPrice,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {}
