package com.supshop.sup_shop_backend.dto.order;

import com.supshop.sup_shop_backend.model.Order;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest (
       @NotNull(message = "{order.status.required}")
       Order.Status status
){}


