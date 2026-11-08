package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.Size;

// перевод из админки. оба поля пустые = убрать перевод, тогда на сайте будет английский оригинал
public record TranslationRequest(
        @Size(max = 150, message = "{translation.name.size}")
        String name,

        String description
) {}
