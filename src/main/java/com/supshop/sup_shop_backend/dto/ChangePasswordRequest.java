package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
        @NotBlank(message = "{password.current.required}")
        String oldPassword,

        @NotBlank(message = "{password.new.required}")
        @Size(min = 8, message = "{password.new.size}")
        String newPassword
){}

