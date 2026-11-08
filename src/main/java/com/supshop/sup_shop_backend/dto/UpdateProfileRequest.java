package com.supshop.sup_shop_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest (
    @Size(max = 150 ,message = "{profile.fullName.size}")
    String fullName,

    @NotBlank(message = "{profile.email.required}")
    @Email(message = "{profile.email.invalid}")
    @Size(max = 150, message = "{profile.email.size}")
    String email
) {}

