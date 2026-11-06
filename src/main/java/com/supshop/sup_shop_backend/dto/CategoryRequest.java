package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "{category.name.required}")
        @Size(max = 150, message = "{category.name.size}")
        String name,

        String description,

        // null — категория верхнего уровня
        Long parentId
) {}
