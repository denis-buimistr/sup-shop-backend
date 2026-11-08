package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "{product.name.required}")
        @Size(max = 200, message = "{product.name.size}")
        String name,

        String description,

        @NotNull(message = "{product.price.required}")
        @DecimalMin(value = "0.0", message = "{product.price.min}")
        BigDecimal price,

        // старая цена, если товар со скидкой; должна быть больше текущей
        BigDecimal compareAtPrice,

        @NotNull(message = "{product.quantity.required}")
        @Min(value = 0, message = "{product.quantity.min}")
        Integer quantity,

        Long brandId,
        Long categoryId,
        String boardType
) {
    @AssertTrue(message = "{product.compareAtPrice.greater}")
    public boolean isCompareAtPriceValid() {
        return compareAtPrice == null || price == null || compareAtPrice.compareTo(price) > 0;
    }
}