package com.supshop.sup_shop_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        // пустой токен бывает только при сломанной ссылке, поэтому ошибка та же, что для протухшей
        @NotBlank(message = "{error.passwordReset.invalidToken}")
        String token,

        @NotBlank(message = "{register.password.required}")
        @Size(min = 8, message = "{register.password.size}")
        String newPassword
) {}