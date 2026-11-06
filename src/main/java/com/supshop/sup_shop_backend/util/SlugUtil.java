package com.supshop.sup_shop_backend.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

public final class SlugUtil {

    private SlugUtil() {
    }

    // «Chișinău Touring 12'6"» -> «chisinau-touring-12-6»: диакритику убираем, всё кроме a-z0-9 превращаем в дефис
    public static String toSlug(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    // nsp-allround занят -> пробуем nsp-allround-2, -3 ...
    public static String uniqueSlug(String text, String fallback, Predicate<String> isTaken) {
        String base = toSlug(text);
        if (base.isEmpty()) {
            base = fallback;
        } else if (base.chars().allMatch(Character::isDigit)) {
            // slug из одних цифр фронт примет за старый id товара (/products/12), поэтому добавляем префикс
            base = fallback + "-" + base;
        }
        String slug = base;
        for (int i = 2; isTaken.test(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }
}
