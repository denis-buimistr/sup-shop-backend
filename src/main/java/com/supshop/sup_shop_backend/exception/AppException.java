package com.supshop.sup_shop_backend.exception;

// ошибка, которую видит покупатель. несу ключ из messages*.properties, а не готовый текст:
// текст на нужном языке собирает GlobalExceptionHandler, сервисам про языки знать не надо
public abstract class AppException extends RuntimeException {

    private final String key;
    private final transient Object[] args;

    protected AppException(String key, Object... args) {
        // в логах вместо текста будет ключ, по нему сразу видно, откуда ошибка
        super(key);
        this.key = key;
        this.args = args;
    }

    public String getKey() {
        return key;
    }

    public Object[] getArgs() {
        return args;
    }
}
