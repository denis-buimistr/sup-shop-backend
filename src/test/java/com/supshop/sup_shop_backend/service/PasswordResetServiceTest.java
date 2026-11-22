package com.supshop.sup_shop_backend.service;

import com.supshop.sup_shop_backend.exception.BadRequestException;
import com.supshop.sup_shop_backend.model.PasswordResetToken;
import com.supshop.sup_shop_backend.model.User;
import com.supshop.sup_shop_backend.repository.PasswordResetTokenRepository;
import com.supshop.sup_shop_backend.repository.UserRepository;
import com.supshop.sup_shop_backend.service.email.EmailMessage;
import com.supshop.sup_shop_backend.service.email.EmailSender;
import com.supshop.sup_shop_backend.service.email.EmailTemplateRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.supshop.sup_shop_backend.support.AppExceptionAssert.assertKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Spring не поднимаю, почту и базу подменяю моками, проверяю только правила сброса
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailSender emailSender;
    @Mock private EmailTemplateRenderer templates;
    @Mock private MessageSource messageSource;

    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(userRepository, tokenRepository, passwordEncoder,
                emailSender, templates, messageSource, FRONTEND_URL);
        user = new User();
        user.setId(1L);
        user.setEmail("ivan@mail.md");
        user.setPasswordHash("old-hash");
        // язык письма берётся из запроса, в тестах ставлю его сам, иначе он зависит от языка системы
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Nested
    class RequestReset {

        @Test
        void unknownEmailSendsNothing() {
            when(userRepository.findByEmailIgnoreCase("ghost@mail.md")).thenReturn(Optional.empty());

            service.requestReset("ghost@mail.md");

            verify(tokenRepository, never()).save(any());
            verify(emailSender, never()).send(any());
        }

        @Test
        void fourthRequestInAnHourSendsNothing() {
            givenUserByEmail();
            when(tokenRepository.countByUserIdAndCreatedAtAfter(eq(1L), any())).thenReturn(3L);

            service.requestReset("ivan@mail.md");

            verify(tokenRepository, never()).save(any());
            verify(emailSender, never()).send(any());
        }

        // в базу уходит только хеш, а сам токен только в письме
        @Test
        void savesHashAndSendsLinkWithRawToken() {
            givenUserByEmail();
            givenEmailTexts();

            service.requestReset(" ivan@mail.md ");

            ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(saved.capture());
            String token = tokenFromSentLink(FRONTEND_URL + "/reset-password?token=");

            assertThat(saved.getValue().getTokenHash())
                    .isEqualTo(sha256(token))
                    .isNotEqualTo(token);
            ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
            verify(templates).render(eq("password-reset"), variables.capture());
            assertThat(variables.getValue()).containsEntry("link", FRONTEND_URL + "/reset-password?token=" + token);
            assertThat(saved.getValue().getExpiresAt())
                    .isBetween(LocalDateTime.now().plusMinutes(29), LocalDateTime.now().plusMinutes(31));
        }

        // старые ссылки гашу до того, как создаю новую, иначе погасла бы и она
        @Test
        void oldLinksAreInvalidatedBeforeNewOneIsSaved() {
            givenUserByEmail();
            givenEmailTexts();

            service.requestReset("ivan@mail.md");

            InOrder order = inOrder(tokenRepository);
            order.verify(tokenRepository).invalidateAll(eq(1L), any());
            order.verify(tokenRepository).save(any());
        }

        @Test
        void linkKeepsLanguageOfTheSite() {
            givenUserByEmail();
            givenEmailTexts();
            LocaleContextHolder.setLocale(Locale.forLanguageTag("ro"));

            service.requestReset("ivan@mail.md");

            tokenFromSentLink(FRONTEND_URL + "/ro/reset-password?token=");
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void setsNewPasswordAndMarksTokenUsed() {
            PasswordResetToken token = new PasswordResetToken(user, "hash", LocalDateTime.now().plusMinutes(10));
            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
            when(passwordEncoder.encode("new-pass-2026")).thenReturn("new-hash");

            service.resetPassword("raw-token", "new-pass-2026");

            assertThat(user.getPasswordHash()).isEqualTo("new-hash");
            assertThat(token.getUsedAt()).isNotNull();
            verify(tokenRepository).invalidateAll(eq(1L), any());
        }

        @Test
        void expiredTokenIsRejected() {
            PasswordResetToken token = new PasswordResetToken(user, "hash", LocalDateTime.now().minusMinutes(1));
            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertKey(() -> service.resetPassword("raw-token", "new-pass-2026"),
                    BadRequestException.class, "error.passwordReset.invalidToken");
            assertThat(user.getPasswordHash()).isEqualTo("old-hash");
        }

        @Test
        void usedTokenIsRejected() {
            PasswordResetToken token = new PasswordResetToken(user, "hash", LocalDateTime.now().plusMinutes(10));
            token.markUsed(LocalDateTime.now().minusMinutes(1));
            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertKey(() -> service.resetPassword("raw-token", "new-pass-2026"),
                    BadRequestException.class, "error.passwordReset.invalidToken");
            verify(tokenRepository, never()).invalidateAll(anyLong(), any());
        }

        @Test
        void unknownTokenIsRejected() {
            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertKey(() -> service.resetPassword("made-up", "new-pass-2026"),
                    BadRequestException.class, "error.passwordReset.invalidToken");
        }
    }

    private void givenUserByEmail() {
        when(userRepository.findByEmailIgnoreCase("ivan@mail.md")).thenReturn(Optional.of(user));
    }

    // вместо настоящих текстов в теле письма просто сама ссылка, так её легко достать
    private void givenEmailTexts() {
        when(messageSource.getMessage(eq("email.passwordReset.subject"), isNull(), any(Locale.class)))
                .thenReturn("Reset");
        when(messageSource.getMessage(eq("email.passwordReset.body"), any(Object[].class), any(Locale.class)))
                .thenAnswer(call -> String.valueOf(call.getArgument(1, Object[].class)[0]));
        when(templates.render(eq("password-reset"), any())).thenReturn("<p>html</p>");
    }

    private String tokenFromSentLink(String expectedPrefix) {
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo("ivan@mail.md");
        assertThat(sent.getValue().subject()).isEqualTo("Reset");
        assertThat(sent.getValue().text()).startsWith(expectedPrefix);
        assertThat(sent.getValue().html()).isEqualTo("<p>html</p>");
        return sent.getValue().text().substring(expectedPrefix.length());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
