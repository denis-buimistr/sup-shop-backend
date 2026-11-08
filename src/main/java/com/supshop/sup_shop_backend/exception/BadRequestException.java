package com.supshop.sup_shop_backend.exception;

public class BadRequestException extends AppException {
    public BadRequestException(String key, Object... args) {
        super(key, args);
    }
}
