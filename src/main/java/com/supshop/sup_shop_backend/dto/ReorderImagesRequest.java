package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// id всех фото товара в новом порядке, первое станет главным
public record ReorderImagesRequest(
        @NotEmpty(message = "{images.order.required}")
        List<@NotNull Long> imageIds
) {}
