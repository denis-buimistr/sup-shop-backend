package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest (
    @Size(max = 150 ,message = "Полное имя: максимум 150 символов")
    String fullName,

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Size(max = 150, message = "Email: максимум 150 символов")
    String email
) {}

