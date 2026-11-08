package com.supshop.sup_shop_backend.support;

import com.supshop.sup_shop_backend.exception.AppException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// общая проверка для тестов сервисов: бросили нужное исключение с нужным ключом.
// сравниваю ключ, а не текст: текст зависит от языка, ключ — нет
public final class AppExceptionAssert {

    private AppExceptionAssert() {
    }

    public static void assertKey(ThrowingCallable call, Class<? extends AppException> type, String key) {
        assertThatThrownBy(call)
                .isInstanceOf(type)
                .extracting(e -> ((AppException) e).getKey())
                .isEqualTo(key);
    }
}
