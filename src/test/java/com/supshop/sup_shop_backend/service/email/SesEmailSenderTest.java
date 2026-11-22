package com.supshop.sup_shop_backend.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

// в настоящий SES не хожу, проверяю только, какой запрос собирается для AWS
@ExtendWith(MockitoExtension.class)
class SesEmailSenderTest {

    private static final String FROM = "SUP Shop <no-reply@supshop-md.site>";

    @Mock private SesV2Client ses;

    private SesEmailSender sender;

    @BeforeEach
    void setUp() {
        sender = new SesEmailSender(ses, FROM);
    }

    @Test
    void plainEmailGoesOnlyAsTextInUtf8() {
        sender.send(EmailMessage.plain("ivan@mail.md", "Сброс пароля", "Привет! Ссылка: https://x"));

        SendEmailRequest request = sentRequest();
        Message message = request.content().simple();
        assertThat(request.fromEmailAddress()).isEqualTo(FROM);
        assertThat(request.destination().toAddresses()).containsExactly("ivan@mail.md");
        assertThat(message.subject().data()).isEqualTo("Сброс пароля");
        assertThat(message.subject().charset()).isEqualTo("UTF-8");
        assertThat(message.body().text().data()).isEqualTo("Привет! Ссылка: https://x");
        assertThat(message.body().html()).isNull();
    }

    @Test
    void htmlVersionIsSentNextToText() {
        sender.send(new EmailMessage("ivan@mail.md", "Hi", "text version", "<p>html version</p>"));

        Message message = sentRequest().content().simple();
        assertThat(message.body().text().data()).isEqualTo("text version");
        assertThat(message.body().html().data()).isEqualTo("<p>html version</p>");
    }

    private SendEmailRequest sentRequest() {
        ArgumentCaptor<SendEmailRequest> request = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(ses).sendEmail(request.capture());
        return request.getValue();
    }
}
