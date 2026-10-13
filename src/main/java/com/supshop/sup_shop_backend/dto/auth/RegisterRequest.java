package com.supshop.sup_shop_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

        @NotBlank(message = "Имя пользователя обязательно")
        @Size(min = 3, max = 50, message = "Имя от 3 до 50 символов")
        String username,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Неккоректный формат email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, message = "Пароль минимум 8 символов")
        String password,

        @Size(max = 150, message = "Полное имя: максимум 150 символов")
        String fullName
) {}

