package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
        @NotBlank(message = "Текущий пароль обязателен")
        String oldPassword,

        @NotBlank(message = "Новый пароль обязателен")
        @Size(min = 8, message = "Новый пароль минимум 8 символов")
        String newPassword
){}

