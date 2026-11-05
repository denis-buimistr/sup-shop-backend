package com.supshop.sup_shop_backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckoutRequest (
    @NotBlank(message = "Адрес доставки обязателен")
    @Size(max = 500, message = "Адрес: максимум 500 символов")
    String shippingAddress
){}

