package com.supshop.sup_shop_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (

        @NotBlank(message = "{login.username.required}")
        String username,

        @NotBlank(message = "{login.password.required}")
        String password
) {}

