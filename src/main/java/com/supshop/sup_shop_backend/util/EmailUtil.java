package com.supshop.sup_shop_backend.util;

import java.util.Locale;

public final class EmailUtil {

    private EmailUtil() {
    }

    // храню почту в одном виде, иначе Ivan@Mail.md и ivan@mail.md станут двумя разными аккаунтами
    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
