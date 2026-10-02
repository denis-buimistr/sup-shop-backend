package com.supshop.sup_shop_backend.dto.auth;

import java.util.List;

public record AuthResponse (
     String token,
     String username,
     List<String> roles

) {}


