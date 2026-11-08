package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest (
    @NotBlank(message = "{brand.name.required}")
    @Size(max = 100, message = "{brand.name.size}")
    String name,

    String description
) {}

