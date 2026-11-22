package com.supshop.sup_shop_backend.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

// локально письма ловит Mailpit по smtp, на проде вместо меня работает SesEmailSender
@Service
@Profile("!prod")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(EmailMessage message) {
        MimeMessage mime = mailSender.createMimeMessage();
        try {
            // multipart, чтобы в одном письме лежали и текст, и html, почтовик сам выберет, что показать
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            if (message.html() != null) {
                helper.setText(message.text(), message.html());
            } else {
                helper.setText(message.text());
            }
        } catch (MessagingException e) {
            throw new MailPreparationException("Could not build email to " + message.to(), e);
        }
        mailSender.send(mime);
    }
}
