package com.supshop.sup_shop_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (

        @NotBlank(message = "{register.username.required}")
        @Size(min = 3, max = 50, message = "{register.username.size}")
        String username,

        @NotBlank(message = "{register.email.required}")
        @Email(message = "{register.email.invalid}")
        String email,

        @NotBlank(message = "{register.password.required}")
        @Size(min = 8, message = "{register.password.size}")
        String password,

        @Size(max = 150, message = "{register.fullName.size}")
        String fullName
) {}

