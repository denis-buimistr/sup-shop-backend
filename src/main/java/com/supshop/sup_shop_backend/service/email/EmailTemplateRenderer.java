package com.supshop.sup_shop_backend.service.email;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

// собираю html письма из шаблона в resources/templates/email. тексты берутся из messages*.properties
// на языке текущего запроса, так что одно и то же письмо уходит на en, ro или ru
@Component
public class EmailTemplateRenderer {

    private final ITemplateEngine templateEngine;

    public EmailTemplateRenderer(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(String template, Map<String, Object> variables) {
        Context context = new Context(LocaleContextHolder.getLocale(), variables);
        return templateEngine.process("email/" + template, context);
    }
}
