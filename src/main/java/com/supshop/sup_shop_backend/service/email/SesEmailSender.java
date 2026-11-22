package com.supshop.sup_shop_backend.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.nio.charset.StandardCharsets;

// на проде шлю через API Amazon SES. ключей нет, права даёт IAM-роль сервера, как для S3
@Service
@Profile("prod")
public class SesEmailSender implements EmailSender {

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    private final SesV2Client ses;
    private final String from;

    public SesEmailSender(SesV2Client ses, @Value("${app.mail.from}") String from) {
        this.ses = ses;
        this.from = from;
    }

    @Override
    public void send(EmailMessage message) {
        Body.Builder body = Body.builder().text(utf8(message.text()));
        if (message.html() != null) {
            body.html(utf8(message.html()));
        }

        ses.sendEmail(SendEmailRequest.builder()
                .fromEmailAddress(from)
                .destination(d -> d.toAddresses(message.to()))
                .content(c -> c.simple(m -> m
                        .subject(utf8(message.subject()))
                        .body(body.build())))
                .build());
    }

    // без явной кодировки кириллица и румынские буквы в письме превращаются в кракозябры
    private static Content utf8(String data) {
        return Content.builder().data(data).charset(CHARSET).build();
    }
}
