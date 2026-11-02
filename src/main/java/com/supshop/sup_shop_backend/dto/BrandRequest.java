package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest (
    @NotBlank(message = "Название бренда обязательно")
    @Size(max = 100, message = "Название: максимум 100 символов")
    String name,

    String description
) {}

