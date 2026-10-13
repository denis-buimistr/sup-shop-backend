package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Название товара обязательно")
        @Size(max = 200, message = "Название: максимум 200 символов")
        String name,

        String description,

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
        BigDecimal price,

        @NotNull(message = "Количество обязательно")
        @Min(value = 0, message = "Количество не может быть отрицательным")
        Integer quantity,

        Long brandId,
        Long categoryId,
        String boardType
) {}