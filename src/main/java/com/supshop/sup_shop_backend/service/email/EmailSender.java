package com.supshop.sup_shop_backend.service.email;

// куда реально уходит письмо, решает реализация: локально smtp в Mailpit, на проде Amazon SES
public interface EmailSender {
    void send(EmailMessage message);
}
