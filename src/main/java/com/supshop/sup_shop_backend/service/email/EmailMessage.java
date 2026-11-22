package com.supshop.sup_shop_backend.service.email;

// одно письмо. html пока может быть null, тогда уйдёт только текст
public record EmailMessage(String to, String subject, String text, String html) {

    public static EmailMessage plain(String to, String subject, String text) {
        return new EmailMessage(to, subject, text, null);
    }
}
