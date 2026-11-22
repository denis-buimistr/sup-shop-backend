package com.supshop.sup_shop_backend.service.email;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// настоящий thymeleaf и настоящие messages*.properties, но без всего Spring: проверяю, что шаблон собирается
class EmailTemplateRendererTest {

    private static final String LINK = "https://supshop-md.site/ru/reset-password?token=abc123";

    private EmailTemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setTemplateEngineMessageSource(messages);
        renderer = new EmailTemplateRenderer(engine);
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void passwordResetIsRenderedInRequestLanguageWithLink() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ru"));

        String html = render();

        assertThat(html)
                .contains("Задать новый пароль")
                .contains("Ссылка работает 30 минут.")
                .contains("href=\"" + LINK + "\"")
                .contains("lang=\"ru\"");
    }

    @Test
    void englishIsUsedByDefault() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        assertThat(render()).contains("Set new password").contains("The link works for 30 minutes.");
    }

    // мои заметки в шаблоне не должны уехать покупателю в исходнике письма
    @Test
    void templateCommentsDoNotReachTheEmail() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        assertThat(render()).doesNotContain("почтовики").doesNotContain("<!--");
    }

    private String render() {
        return renderer.render("password-reset", Map.of(
                "link", LINK,
                "minutes", 30L,
                "siteUrl", "https://supshop-md.site"));
    }
}
