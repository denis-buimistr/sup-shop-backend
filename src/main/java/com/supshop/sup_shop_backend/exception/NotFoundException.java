package com.supshop.sup_shop_backend.exception;

public class NotFoundException extends AppException {
    public NotFoundException(String key, Object... args) {
        super(key, args);
    }
}
