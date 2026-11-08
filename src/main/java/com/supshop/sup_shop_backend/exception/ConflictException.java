package com.supshop.sup_shop_backend.exception;

public class ConflictException extends AppException {
    public ConflictException(String key, Object... args) {
        super(key, args);
    }
}
