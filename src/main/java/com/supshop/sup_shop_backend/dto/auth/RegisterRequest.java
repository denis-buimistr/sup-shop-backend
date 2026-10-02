package com.supshop.sup_shop_backend.dto.auth;

public record RegisterRequest (
    String username,
    String email,
    String password,
    String fullName
) {}

