package com.supshop.sup_shop_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        List<String> roles,
        LocalDateTime createdAt
) {}