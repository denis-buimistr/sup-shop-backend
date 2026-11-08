package com.supshop.sup_shop_backend.util;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Set;

// язык контента для текущего запроса. его кладёт LocaleResolver из WebConfig по заголовку Accept-Language
public final class ContentLanguage {

    public static final String DEFAULT = "en";
    // английский лежит в основных таблицах, для этих языков есть таблицы *_translations
    private static final Set<String> TRANSLATED = Set.of("ro", "ru");

    private ContentLanguage() {
    }

    public static boolean isTranslated(String lang) {
        return TRANSLATED.contains(lang);
    }

    public static String current() {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return TRANSLATED.contains(lang) ? lang : DEFAULT;
    }
}
