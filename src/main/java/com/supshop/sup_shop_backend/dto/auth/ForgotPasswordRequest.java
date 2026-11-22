package com.supshop.sup_shop_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "{register.email.required}")
        @Email(message = "{register.email.invalid}")
        String email
) {}